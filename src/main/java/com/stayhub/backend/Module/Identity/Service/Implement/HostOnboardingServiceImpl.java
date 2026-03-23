package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Util.CancellationPolicy;
import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Common.Util.SlugUtils;
import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Identity.Service.HostOnboardingService;
import com.stayhub.backend.Module.Property.Model.*;
import com.stayhub.backend.Module.Property.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HostOnboardingServiceImpl implements HostOnboardingService {
    private final HostDetailRepository hostDetailRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final CategoryRepository categoryRepository;
    private final RentalTypeRepository rentalTypeRepository;
    private final AmenityRepository amenityRepository;
    private final PromotionRepository promotionRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitHostApplication(String email, HostVerificationRequest request) {

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng!"));

        HostDetail hostDetail = hostDetailRepository.findById(currentUser.getId())
                .orElse(HostDetail.builder()
                        .user(currentUser)
                        .build());

        hostDetail.setBusinessPhone(request.businessPhone());
        hostDetail.setSupportEmail(request.supportEmail());
        hostDetail.setIdentityCardNumber(request.identityCardNumber());
        hostDetail.setIdentityCardFrontUrl(request.identityCardFrontUrl());
        hostDetail.setIdentityCardBackUrl(request.identityCardBackUrl());

        hostDetail.setBusinessLicenseNumber(request.businessLicenseNumber());
        hostDetail.setBusinessLicenseUrl(request.businessLicenseUrl());

        hostDetail.setOnboardingStatus(HostOnboardingStatus.PENDING_REVIEW);

        hostDetailRepository.save(hostDetail);
    }
}
