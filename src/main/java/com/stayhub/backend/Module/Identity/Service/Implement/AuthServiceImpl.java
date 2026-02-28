package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Util.ErrorCode;
import com.stayhub.backend.Common.Util.UserStatus;
import com.stayhub.backend.Common.Util.VerificationType;
import com.stayhub.backend.Module.Identity.DTO.Request.RegisterGuestRequest;
import com.stayhub.backend.Module.Identity.Model.Profile;
import com.stayhub.backend.Module.Identity.Model.Role;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Model.VerificationToken;
import com.stayhub.backend.Module.Identity.Repository.ProfileRepository;
import com.stayhub.backend.Module.Identity.Repository.RoleRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Identity.Repository.VerificationTokenRepository;
import com.stayhub.backend.Module.Identity.Service.AuthService;
import com.stayhub.backend.Module.Identity.Service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
}
