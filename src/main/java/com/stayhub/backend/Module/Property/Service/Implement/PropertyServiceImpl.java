package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.*;
import com.stayhub.backend.Module.Identity.DTO.Response.CancellationPolicyResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.HostInfoResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.PropertyDetailResponse;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyCardResponse;
import com.stayhub.backend.Module.Property.Model.*;
import com.stayhub.backend.Module.Property.Repository.*;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        HostDetail hostDetail = hostDetailRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.HOST_PROFILE_NOT_FOUND));

        if (hostDetail.getOnboardingStatus() != HostOnboardingStatus.APPROVED) {
            throw new AppException(ErrorCode.HOST_NOT_APPROVED);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục nhà!"));

        RentalType rentalType = rentalTypeRepository.findById(request.rentalTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại hình cho thuê!"));

        CancellationPolicy policy = cancellationPolicyRepository.findById(request.cancellationPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chính sách hủy phòng!"));

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
                .slug(SlugUtils.toSlug(request.name() + "-" + System.currentTimeMillis()))

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

    @Override
    public PageResponse<PropertyCardResponse> getPropertiesForGuest(int page, int size, String sortBy, String sortDir, String destination, Integer guestCount) {
        Pageable pageable = PaginationUtil.getPageable(page, size, sortBy, sortDir);
        Specification<Property> spec = PropertySpecification.buildSearchFilter(destination, guestCount);
        Page<Property> propertyPage = propertyRepository.findAll(spec, pageable);

        List<PropertyCardResponse> cardResponses = propertyPage.stream().map(property -> {
            String thumbnailUrl = property.getImages().stream()
                    .filter(PropertyImage::getIsThumbnail)
                    .map(PropertyImage::getUrl)
                    .findFirst()
                    .orElse(null);

            List<String> amenityNames = property.getAmenities().stream()
                    .map(Amenity::getName)
                    .toList();

            return new PropertyCardResponse(
                    property.getId(),
                    property.getName(),
                    property.getSlug(),
                    property.getProvince(),
                    property.getDistrict(),
                    property.getPricePerNight(),
                    thumbnailUrl,
                    property.getRatingAvg(),
                    property.getMaxGuests(),
                    property.getNumBedrooms(),
                    property.getNumBeds(),
                    property.getNumBathrooms(),
                    amenityNames
            );
        }).toList();

        return PageResponse.<PropertyCardResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPage(propertyPage.getTotalPages())
                .totalElements(propertyPage.getTotalElements())
                .items(cardResponses)
                .build();
    }

    @Override
    public PropertyDetailResponse getPropertyBySlug(String slug) {
        Property property = propertyRepository.findBySlugAndStatus(slug, PropertyStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỗ ở này hoặc bài đăng chưa được duyệt!"));

        List<String> amenityNames = property.getAmenities().stream()
                .map(Amenity::getName)
                .toList();

        List<String> imageUrls = property.getImages().stream()
                .sorted(Comparator.comparing(PropertyImage::getDisplayOrder))
                .map(PropertyImage::getUrl)
                .toList();

        User host = property.getHost();
        String hostName = host.getEmail();
        String avatarUrl = null;

        if (host.getProfile() != null) {
            hostName = host.getProfile().getFullName();
            avatarUrl = host.getProfile().getAvatarUrl();
        }

        HostInfoResponse hostInfo = HostInfoResponse.builder()
                .id(host.getId())
                .fullName(hostName)
                .avatarUrl(avatarUrl)
                .joinedAt(host.getHostDetail().getCreatedAt())
                .build();

        CancellationPolicyResponse cancellationPolicy = CancellationPolicyResponse.builder()
                .id(property.getCancellationPolicy().getId())
                .name(property.getCancellationPolicy().getName())
                .description(property.getCancellationPolicy().getDescription())
                .refundPercentage(property.getCancellationPolicy().getRefundPercentage())
                .daysBeforeCheckin(property.getCancellationPolicy().getDaysBeforeCheckin())
                .build();

        return PropertyDetailResponse.builder()
                .id(property.getId())
                .name(property.getName())
                .slug(property.getSlug())
                .description(property.getDescription())

                .addressDetail(property.getAddressDetail())
                .ward(property.getWard())
                .district(property.getDistrict())
                .province(property.getProvince())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())

                .maxGuests(property.getMaxGuests())
                .numBedrooms(property.getNumBedrooms())
                .numBeds(property.getNumBeds())
                .numBathrooms(property.getNumBathrooms())

                .pricePerNight(property.getPricePerNight())
                .cleaningFee(property.getCleaningFee())
                .weekendSurchargePercentage(property.getWeekendSurchargePercentage())
                .depositPercentage(property.getDepositPercentage())
                .isPayAtCheckinAllowed(property.getIsPayAtCheckinAllowed())

                .checkinAfter(property.getCheckinAfter())
                .checkoutBefore(property.getCheckoutBefore())
                .isInstantBook(property.getIsInstantBook())
                .isSmokingAllowed(property.getIsSmokingAllowed())
                .isPetsAllowed(property.getIsPetsAllowed())
                .isPartyAllowed(property.getIsPartyAllowed())

                .ratingAvg(property.getRatingAvg())
                .reviewCount(property.getReviewCount())

                .categoryName(property.getCategory().getName())
                .rentalTypeName(property.getRentalType().getName())

                .host(hostInfo)
                .cancellationPolicy(cancellationPolicy)

                .amenities(amenityNames)
                .imageUrls(imageUrls)
                .build();
    }
}
