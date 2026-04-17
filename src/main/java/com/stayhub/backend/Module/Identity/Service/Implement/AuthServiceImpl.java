package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Util.ErrorCode;
import com.stayhub.backend.Common.Util.UserStatus;
import com.stayhub.backend.Common.Util.VerificationType;
import com.stayhub.backend.Module.Identity.DTO.Request.LoginRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.LogoutRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.RefreshTokenRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.RegisterGuestRequest;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.TokenRefreshResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.UserInfResponse;
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
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

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
        tokenRepository.save(verificationToken);

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
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, VerificationType.REGISTER)
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
        tokenRepository.save(verificationToken);

        return "Xác thực tài khoản thành công. Bạn có thể đăng nhập ngay bây giờ.";
    }

    @Transactional(rollbackFor = Exception.class)
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

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElse(new Profile());

        UserInfResponse userInfResponse = new UserInfResponse(
                user.getEmail(),
                profile.getFullName(),
                profile.getAvatarUrl()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        refreshTokenRepository.deleteByUserAndDeviceInfo(user, deviceInfo);

        String refreshTokenString = UUID.randomUUID().toString();
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

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponse(
                accessToken,
                refreshTokenString,
                roles,
                user.getStatus().name(),
                userInfResponse
        );
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
}
