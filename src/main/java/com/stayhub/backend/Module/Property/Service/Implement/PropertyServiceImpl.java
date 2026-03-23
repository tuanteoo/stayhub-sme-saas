package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.Model.*;
import com.stayhub.backend.Module.Property.Repository.*;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final UserRepository userRepository;
    private final HostDetailRepository hostDetailRepository;
    private final PropertyRepository propertyRepository;
    private final CategoryRepository categoryRepository;
    private final RentalTypeRepository rentalTypeRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final AmenityRepository amenityRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProperty(String hostEmail, PropertyCreateRequest request) {
        User currentUser = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        HostDetail hostDetail = hostDetailRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Bạn phải đăng ký làm Chủ nhà trước khi đăng tin!"));

        if (hostDetail.getOnboardingStatus() != HostOnboardingStatus.APPROVED) {
            throw new RuntimeException("Hồ sơ Chủ nhà của bạn chưa được duyệt! Chỉ những Chủ nhà đã được xác thực mới có quyền đăng tin.");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục nhà!"));

        RentalType rentalType = rentalTypeRepository.findById(request.rentalTypeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại hình cho thuê!"));

        CancellationPolicy policy = cancellationPolicyRepository.findById(request.cancellationPolicyId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chính sách hủy phòng!"));

        Property property = Property.builder()
                .host(currentUser)
                .category(category)
                .rentalType(rentalType)
                .cancellationPolicy(policy)

                // Address
                .province(request.province())
                .district(request.district())
                .ward(request.ward())
                .addressDetail(request.addressDetail())

                // Structure
                .maxGuests(request.maxGuests())
                .numBedrooms(request.numBedrooms())
                .numBeds(request.numBeds())
                .numBathrooms(request.numBathrooms())

                // Content
                .name(request.name())
                .description(request.description())
                .slug(generateSlug(request.name()+ "-" + System.currentTimeMillis()))

                // Price and Payment
                .pricePerNight(request.pricePerNight())
                .weekendSurchargePercentage(request.weekendSurchargePercentage())
                .cleaningFee(request.cleaningFee())
                .isPayAtCheckinAllowed(request.isPayAtCheckinAllowed())
                .depositPercentage(request.isPayAtCheckinAllowed() ? request.depositPercentage() : 100)

                // Status Default
                .status(PropertyStatus.PENDING_REVIEW)
                .build();

        List<Amenity> amenityList = amenityRepository.findAllById(request.amenityIds());
        property.getAmenities().addAll(amenityList);

        Set<PropertyImage> images = new HashSet<>();
        List<String> urls = request.imageUrls();
        for (int i = 0; i < urls.size(); i++) {
            images.add(PropertyImage.builder()
                    .property(property)
                    .url(urls.get(i))
                    .displayOrder(i)
                    .isThumbnail(i == 0)
                    .build());
        }
        property.getImages().addAll(images);

        propertyRepository.save(property);
    }

    private String generateSlug(String name) {
        if (name == null) return "";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return slug.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
