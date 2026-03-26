package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.ErrorCode;
import com.stayhub.backend.Common.Util.HostOnboardingStatus;
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
import com.stayhub.backend.Module.Property.Repository.*;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class HostOnboardingServiceImpl implements HostOnboardingService {
    private final HostDetailRepository hostDetailRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PropertyService propertyService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
    public void submitHostApplication(String email, HostRegistrationWithPropertyRequest request) {

        User currentUser = userRepository.findByEmail(email)
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

        propertyService.createProperty(email, request.firstProperty());
    }

    @Override
    public void reviewHostApplication(Long userId, HostApprovalRequest request) {
        HostDetail hostDetail = hostDetailRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.HOST_PROFILE_NOT_FOUND));

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

            propertyService.approveFirstPendingPropertyByHost(userId);
        }

        hostDetailRepository.save(hostDetail);
    }
}
