package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.*;
import com.stayhub.backend.Module.Identity.DTO.Request.*;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.TokenRefreshResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.UserAdminResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.UserProfileResponse;
import com.stayhub.backend.Module.Identity.Model.*;
import com.stayhub.backend.Module.Identity.Repository.*;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetailsService;
import com.stayhub.backend.Module.Identity.Security.JwtTokenProvider;
import com.stayhub.backend.Module.Identity.Service.AuthService;
import com.stayhub.backend.Common.Service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final HostDetailRepository hostDetailRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerGuest(RegisterGuestRequest request) {
        Optional<User> existingUserOpt = userRepository.findByEmail(request.email());
        User user;
        Profile profile;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();

            if (user.getStatus() != UserStatus.UNVERIFIED) {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }

            user.setPassword(passwordEncoder.encode(request.password()));
            userRepository.save(user);

            profile = profileRepository.findById(user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
            profile.setFullName(request.fullName());
            profileRepository.save(profile);

        } else {
            Role guestRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            user = User.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .status(UserStatus.UNVERIFIED)
                    .roles(Set.of(guestRole))
                    .build();
            user = userRepository.save(user);

            profile = Profile.builder()
                    .user(user)
                    .fullName(request.fullName())
                    .build();
            profileRepository.save(profile);
        }

        String tokenString = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(tokenString)
                .type(VerificationType.REGISTER)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmailAsync(
                user.getEmail(),
                profile.getFullName(),
                verificationToken.getToken()
        );
        log.info("User {} đăng ký thành công. Đang gửi email xác thực ngầm...", user.getEmail());
        return "Đăng ký tài khoản thành công. Vui lòng kiểm tra email để xác thực.";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String verifyEmailToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndType(token, VerificationType.REGISTER)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (verificationToken.getConfirmedAt() != null) {
            throw new AppException(ErrorCode.USER_ALREADY_VERIFIED);
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        verificationToken.setConfirmedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);

        return "Xác thực tài khoản thành công. Bạn có thể đăng nhập ngay bây giờ.";
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = httpRequest.getRemoteAddr();
        }
        String deviceInfo = httpRequest.getHeader("User-Agent");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorCode.USER_BANNED);
        } else if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new AppException(ErrorCode.USER_UNVERIFIED);
        }

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(new Profile());
        UserProfileResponse userInfResponse = UserProfileResponse.builder()
                .email(user.getEmail())
                .avatarUrl(user.getProfile().getAvatarUrl())
                .fullName(user.getProfile().getFullName())
                .build();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshTokenString = UUID.randomUUID().toString();

        saveLoginSession(user, ipAddress, deviceInfo, refreshTokenString);

        return new LoginResponse(
                accessToken,
                refreshTokenString,
                roles,
                user.getStatus().name(),
                userInfResponse
        );
    }

    @Transactional(rollbackFor = Exception.class)
    protected void saveLoginSession(User user, String ipAddress, String deviceInfo, String refreshTokenString) {
        refreshTokenRepository.deleteOldTokens(user, deviceInfo);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .build();
        refreshTokenRepository.save(refreshToken);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void logout(LogoutRequest request) {
        boolean tokenExists = refreshTokenRepository.findByToken(request.refreshToken()).isPresent();

        if (!tokenExists) {
            log.warn("Cố gắng đăng xuất với Refresh Token không tồn tại hoặc đã bị xóa.");
        } else {
            refreshTokenRepository.deleteByToken(request.refreshToken());

            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("User {} đã đăng xuất và xóa Refresh Token thành công", currentUserEmail);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.refreshToken();
        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(token -> {
                    if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                        refreshTokenRepository.delete(token);
                        throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
                    }
                    return token;
                })
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

                    return new TokenRefreshResponse(newAccessToken, requestRefreshToken);
                })
                .orElseThrow(() -> new RuntimeException("Refresh Token không hợp lệ hoặc không tồn tại trong hệ thống."));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này"));

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .type(VerificationType.FORGOT_PASSWORD)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        verificationTokenRepository.save(verificationToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getProfile().getFullName(), token);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndType(request.token(), VerificationType.FORGOT_PASSWORD)
                .orElseThrow(() -> new InvalidDataException("Mã xác thực không hợp lệ hoặc không tồn tại"));

        if (verificationToken.getType() != VerificationType.FORGOT_PASSWORD) {
            throw new InvalidDataException("Mã xác thực không đúng định dạng");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("Mã xác thực đã hết hạn");
        }

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        Profile profile = user.getProfile();
        HostDetail hostDetail = user.getHostDetail();

        String hostCode = null;
        String maskedIdCard = null;
        String maskedBusinessLicense = null;
        String businessPhone = null;
        String supportEmail = null;
        String onboardingStatus = null;
        LocalDateTime joinedAt = user.getCreatedAt();

        if (hostDetail != null) {
            hostCode = hostDetail.getHostCode();
            businessPhone = hostDetail.getBusinessPhone();
            supportEmail = hostDetail.getSupportEmail();
            onboardingStatus = hostDetail.getOnboardingStatus() != null ? hostDetail.getOnboardingStatus().name() : null;
            joinedAt = hostDetail.getCreatedAt();

            maskedIdCard = StringUtil.maskString(hostDetail.getIdentityCardNumber(), 4, 3);
            maskedBusinessLicense = StringUtil.maskString(hostDetail.getBusinessLicenseNumber(), 3, 3);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .avatarUrl(profile.getAvatarUrl())
                .gender(profile.getGender())
                .dateOfBirth(profile.getDob())
                .addressDetail(profile.getAddressDetail())
                .bio(profile.getBio())

                .hostCode(hostCode)
                .businessPhone(businessPhone)
                .supportEmail(supportEmail)
                .onboardingStatus(onboardingStatus)
                .maskedIdentityCard(maskedIdCard)
                .maskedBusinessLicense(maskedBusinessLicense)
                .joinedAt(joinedAt)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        Profile profile = user.getProfile();

        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
        }

        if (request.fullName() != null) {
            profile.setFullName(request.fullName());
        }
        if (request.phoneNumber() != null) {
            profile.setPhoneNumber(request.phoneNumber());
        }
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
        }
        if (request.gender() != null) {
            profile.setGender(request.gender());
        }
        if (request.dateOfBirth() != null) {
            profile.setDob(request.dateOfBirth());
        }
        if (request.address() != null) {
            profile.setAddressDetail(request.address());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }

        profileRepository.save(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateHostProfile(Long userId, UpdateHostProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        if (user.getHostDetail() == null) {
            throw new InvalidDataException("Tài khoản của bạn chưa được đăng ký làm Chủ nhà.");
        }

        if (request.businessPhone() != null && !request.businessPhone().isBlank()) {
            user.getHostDetail().setBusinessPhone(request.businessPhone());
        }

        if (request.supportEmail() != null && !request.supportEmail().isBlank()) {
            user.getHostDetail().setSupportEmail(request.supportEmail());
        }

        hostDetailRepository.save(user.getHostDetail());

        if (request.updateProfileRequest() != null) {
            this.updateUserProfile(userId, request.updateProfileRequest());
        }

        log.info("Chủ nhà ID {} đã cập nhật thông tin hồ sơ thành công", userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng!"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidDataException("Mật khẩu hiện tại không chính xác.");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new InvalidDataException("Mật khẩu xác nhận không khớp.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidDataException("Mật khẩu mới không được giống mật khẩu hiện tại.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenRepository.deleteByUser(user);

        log.info("Người dùng ID {} đã thay đổi mật khẩu và bị thu hồi các phiên đăng nhập cũ.", userId);
    }

    @Override
    public PageResponse<UserAdminResponse> getUsersForAdmin(String status, int pageNo, int pageSize, String sortBy, String sortDir) {
        Pageable pageable = PaginationUtil.getPageable(pageNo, pageSize, sortBy, sortDir, "createdAt");

        UserStatus userStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                userStatus = UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("Trạng thái người dùng không hợp lệ.");
            }
        }

        Page<User> userPage = userRepository.findAllUsersForAdmin(userStatus, pageable);

        List<UserAdminResponse> responses = userPage.getContent().stream()
                .map(user -> {
                    List<String> roleNames = user.getRoles().stream()
                            .map(Role::getName)
                            .toList();

                    return UserAdminResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getProfile() != null ? user.getProfile().getFullName() : "N/A")
                            .phoneNumber(user.getProfile() != null ? user.getProfile().getPhoneNumber() : "N/A")
                            .avatarUrl(user.getProfile() != null ? user.getProfile().getAvatarUrl() : null)
                            .roles(roleNames)
                            .status(user.getStatus().name())
                            .lastLoginAt(user.getLastLoginAt())
                            .createdAt(user.getCreatedAt())
                            .build();
                })
                .toList();

        return PageResponse.<UserAdminResponse>builder()
                .pageNo(userPage.getNumber() + 1)
                .pageSize(userPage.getSize())
                .totalPage(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .items(responses)
                .build();
    }
}
