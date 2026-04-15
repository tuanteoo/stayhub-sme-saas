package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Service.EmailService;
import com.stayhub.backend.Common.Util.*;
import com.stayhub.backend.Module.Finance.Model.Wallet;
import com.stayhub.backend.Module.Finance.Repository.WalletRepository;
import com.stayhub.backend.Module.Identity.DTO.Request.HostApprovalRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostRegistrationWithPropertyRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.Role;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.RoleRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Identity.Service.HostOnboardingService;
import com.stayhub.backend.Module.Property.Model.SubscriptionPlan;
import com.stayhub.backend.Module.Property.Model.UserSubscription;
import com.stayhub.backend.Module.Property.Repository.*;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostOnboardingServiceImpl implements HostOnboardingService {
    private final HostDetailRepository hostDetailRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PropertyService propertyService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final EmailService emailService;
    private final WalletRepository walletRepository;

    private String generateRandomBlock(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(SECURE_RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String generateUniqueHostCode() {
        String code;
        do {
            code = "HOU-" + generateRandomBlock(3) + "-"
                    + generateRandomBlock(3) + "-"
                    + generateRandomBlock(3);
        } while (hostDetailRepository.existsByHostCode(code));
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitHostApplication(Long hostId, HostRegistrationWithPropertyRequest request) {

        User currentUser = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản người dùng!"));

        HostDetail hostDetail = hostDetailRepository.findById(currentUser.getId())
                .orElse(HostDetail.builder()
                        .user(currentUser)
                        .build());

        if (hostDetail.getHostCode() == null) {
            hostDetail.setHostCode(generateUniqueHostCode());
        }

        var hostReq = request.hostDetails();

        hostDetail.setBusinessPhone(hostReq.businessPhone());
        hostDetail.setSupportEmail(hostReq.supportEmail());
        hostDetail.setIdentityCardNumber(hostReq.identityCardNumber());
        hostDetail.setIdentityCardFrontUrl(hostReq.identityCardFrontUrl());
        hostDetail.setIdentityCardBackUrl(hostReq.identityCardBackUrl());
        hostDetail.setBusinessLicenseNumber(hostReq.businessLicenseNumber());
        hostDetail.setBusinessLicenseUrl(hostReq.businessLicenseUrl());

        hostDetail.setOnboardingStatus(HostOnboardingStatus.PENDING_REVIEW);
        hostDetailRepository.save(hostDetail);

        propertyService.createProperty(hostId, request.firstProperty());

        return hostDetail.getHostCode();
    }

    @Override
    public void reviewHostApplication(String hostCode, HostApprovalRequest request) {
        HostDetail hostDetail = hostDetailRepository.findByHostCode(hostCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ đăng ký host!"));

        hostDetail.setOnboardingStatus(request.status());

        if (request.status() == HostOnboardingStatus.REJECTED || request.status() == HostOnboardingStatus.REQUEST_CHANGES) {
            hostDetail.setReviewNote(request.reviewNote());
        } else {
            hostDetail.setReviewNote(null);
        }

        if (request.status() == HostOnboardingStatus.APPROVED) {
            User user = hostDetail.getUser();

            Role hostRole = roleRepository.findByName("ROLE_HOST")
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi hệ thống: Không tìm thấy quyền hợp lệ"));
            user.getRoles().add(hostRole);

            userRepository.save(user);
            log.info("Đã gán ROLE_HOST cho User {} sau khi duyệt hồ sơ đăng ký", user.getId());

            propertyService.approveFirstPendingPropertyByHost(hostDetail.getId());

            SubscriptionPlan freePlan = subscriptionPlanRepository.findByTier(SubscriptionTier.FREE)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói cước FREE cấu hình trong hệ thống"));

            UserSubscription userSubscription = UserSubscription.builder()
                    .user(user)
                    .plan(freePlan)
                    .status(UserSubscriptionStatus.ACTIVE)
                    .autoRenew(true)
                    .currentCommissionRate(freePlan.getCommissionRate())
                    .currentMaxListings(freePlan.getMaxListings())
                    .currentCreditLimit(freePlan.getCreditLimit())
                    .build();
            userSubscriptionRepository.save(userSubscription);

            log.info("Đã kích hoạt gói cước FREE cho Host {} sau khi duyệt hồ sơ đăng ký", user.getId());

            if (!walletRepository.existsByUser_Id(hostDetail.getId())){
                walletRepository.save(Wallet.builder()
                                .user(user)
                                .availableBalance(BigDecimal.ZERO)
                                .pendingBalance(BigDecimal.ZERO)
                                .debtBalance(BigDecimal.ZERO)
                                .currency("VND")
                                .status(WalletStatus.ACTIVE)
                        .build());
                log.info("Đã khởi tạo ví cho Host {} sau khi duyệt hồ sơ đăng ký", user.getId());
            }

            emailService.sendHostApprovalEmail(user.getEmail(), user.getProfile().getFullName());
        }

        hostDetailRepository.save(hostDetail);
    }
}
