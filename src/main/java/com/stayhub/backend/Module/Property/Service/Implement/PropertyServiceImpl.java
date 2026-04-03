package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.*;
import com.stayhub.backend.Module.Property.DTO.Response.*;
import com.stayhub.backend.Module.Identity.DTO.Response.HostInfoResponse;
import com.stayhub.backend.Module.Property.DTO.Request.RoomCreateRequest;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.Mapper.AmenityMapper;
import com.stayhub.backend.Module.Property.Mapper.RoomMapper;
import com.stayhub.backend.Module.Property.Model.*;
import com.stayhub.backend.Module.Property.Repository.*;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    private final RoomMapper roomMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProperty(String email, PropertyCreateRequest request) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        HostDetail hostDetail = hostDetailRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.HOST_PROFILE_NOT_FOUND));

        if (hostDetail.getOnboardingStatus() != HostOnboardingStatus.APPROVED &&
                hostDetail.getOnboardingStatus() != HostOnboardingStatus.PENDING_REVIEW) {
            throw new AppException(ErrorCode.HOST_NOT_APPROVED);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục nhà!"));

        RentalType rentalType = rentalTypeRepository.findById(request.rentalTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại hình cho thuê!"));

        int finalRoomCount = 1;
        if ("toan-bo-cho-o".equals(rentalType.getSlug())) {
            if (request.rooms() == null || request.rooms().size() != 1) {
                throw new InvalidDataException("Loại hình 'Toàn bộ chỗ ở' chỉ được phép khai báo 1 phòng duy nhất (đại diện cho toàn bộ căn nhà)!");
            }
            if (request.roomCount() == null || request.roomCount() < 1) {
                throw new InvalidDataException("Vui lòng nhập số lượng phòng của căn nhà!");
            }
            finalRoomCount = request.roomCount();
        }else  {
            if (request.rooms() == null || request.rooms().isEmpty()) {
                throw new InvalidDataException("Vui lòng thêm ít nhất 1 phòng cho chỗ ở của bạn!");
            }
            finalRoomCount = request.rooms().size();
        }

        Property property = Property.builder()
                .host(currentUser)
                .category(category)
                .rentalType(rentalType)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .province(request.province())
                .district(request.district())
                .ward(request.ward())
                .addressDetail(request.addressDetail())
                .name(request.name())
                .description(request.description())
                .slug(SlugUtils.toSlug(request.name() + "-" + System.currentTimeMillis()))
                .weekendSurchargePercentage(request.weekendSurchargePercentage())
                .cleaningFee(request.cleaningFee())
                .roomCount(finalRoomCount)
                .status(PropertyStatus.PENDING_REVIEW)
                .build();

        Set<Long> allAmenityIds = new HashSet<>();
        if (request.amenityIds() != null) {
            allAmenityIds.addAll(request.amenityIds());
        }

        if (request.rooms() != null) {
            request.rooms().stream()
                    .filter(r -> r.amenityIds() != null)
                    .flatMap(r -> r.amenityIds().stream())
                    .forEach(allAmenityIds::add);
        }

        Map<Long, Amenity> amenityMap = new HashMap<>();
        if (!allAmenityIds.isEmpty()) {
            amenityRepository.findAllById(allAmenityIds)
                    .forEach(amenity -> amenityMap.put(amenity.getId(), amenity));

            if (amenityMap.size() != allAmenityIds.size()) {
                throw new InvalidDataException("Một hoặc nhiều tiện ích không tồn tại trong hệ thống!");
            }
        }

        if (request.amenityIds() != null) {
            Set<Amenity> propertyAmenities = request.amenityIds().stream()
                    .map(amenityMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            property.getAmenities().addAll(propertyAmenities);
        }

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            IntStream.range(0, request.imageUrls().size())
                    .forEach(i -> property.getImages().add(PropertyImage.builder()
                            .property(property)
                            .url(request.imageUrls().get(i))
                            .displayOrder(i)
                            .isThumbnail(i == 0)
                            .build()));
        }

        Set<Long> propertyAmenityIds = request.amenityIds() != null
                ? new HashSet<>(request.amenityIds())
                : Collections.emptySet();

        if (request.rooms() != null) {
            List<Room> rooms = request.rooms().stream().map(roomReq -> {

                Room room = Room.builder()
                        .property(property)
                        .name(roomReq.name())
                        .description(roomReq.description())
                        .pricePerNight(roomReq.pricePerNight())
                        .maxGuests(roomReq.maxGuests())
                        .numBeds(roomReq.numBeds())
                        .numBathrooms(roomReq.numBathrooms())
                        .build();

                if (roomReq.amenityIds() != null) {
                    Set<Amenity> roomAmenities = roomReq.amenityIds().stream()
                            .filter(id -> !propertyAmenityIds.contains(id))
                            .map(amenityMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    room.getAmenities().addAll(roomAmenities);
                }

                if (roomReq.imageUrls() != null && !roomReq.imageUrls().isEmpty()) {
                    IntStream.range(0, roomReq.imageUrls().size())
                            .forEach(i -> room.getImages().add(RoomImage.builder()
                                    .room(room)
                                    .url(roomReq.imageUrls().get(i))
                                    .displayOrder(i)
                                    .isThumbnail(i == 0)
                                    .build()));
                }

                LocalDate today = LocalDate.now();
                List<RoomAvailability> availabilities = java.util.stream.IntStream.range(0, 365)
                        .mapToObj(i -> RoomAvailability.builder()
                                .room(room)
                                .date(today.plusDays(i))
                                .isAvailable(true)
                                .priceModifier(BigDecimal.ZERO)
                                .build())
                        .toList();
                room.getAvailabilities().addAll(availabilities);

                return room;
            }).toList();

            property.getRooms().addAll(rooms);
        }

        propertyRepository.save(property);
    }

    @Override
    public PageResponse<HostPropertyResponse> getPropertiesByHost(Long id, int page, int size, String sortBy, String sortDir) {
        User host = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Pageable pageable = PaginationUtil.getPageable(page, size, sortBy, sortDir);
        Page<Property> propertyPage = propertyRepository.findByHostId(host.getId(), pageable);
        List<HostPropertyResponse> responses = propertyPage.stream().map(property -> {
            // Lấy ảnh Thumbnail
            String thumbnailUrl = property.getImages().stream()
                    .filter(PropertyImage::getIsThumbnail)
                    .map(PropertyImage::getUrl)
                    .findFirst()
                    .orElse(null);

            BigDecimal startingPrice = property.getRooms().stream()
                    .map(Room::getPricePerNight)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            return new HostPropertyResponse(
                    property.getId(),
                    property.getName(),
                    property.getSlug(),
                    property.getAddressDetail(),
                    property.getProvince(),
                    startingPrice,
                    thumbnailUrl,
                    property.getStatus(),
                    property.getRatingAvg(),
                    property.getReviewCount(),
                    property.getCreatedAt()
            );
        }).toList();
        return PageResponse.<HostPropertyResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPage(propertyPage.getTotalPages())
                .totalElements(propertyPage.getTotalElements())
                .items(responses)
                .build();
    }

    @Override
    public PageResponse<PropertyCardResponse> getPropertiesForGuest(int page, int size, String sortBy, String sortDir, String destination, Integer guestCount, LocalDate checkInDate, LocalDate checkOutDate) {
        Pageable pageable = PaginationUtil.getPageable(page, size, sortBy, sortDir);
        Specification<Property> spec = PropertySpecification.buildSearchFilter(destination, guestCount, checkInDate, checkOutDate);
        Page<Property> propertyPage = propertyRepository.findAll(spec, pageable);

        List<PropertyCardResponse> cardResponses = propertyPage.stream().map(property -> {
            String thumbnailUrl = property.getImages().stream()
                    .filter(PropertyImage::getIsThumbnail)
                    .map(PropertyImage::getUrl)
                    .findFirst()
                    .orElse(null);

            Set<String> allAmenityNames = Stream.concat(
                            property.getAmenities().stream(),
                            property.getRooms().stream().flatMap(room -> room.getAmenities().stream())
                    )
                    .map(Amenity::getName)
                    .collect(Collectors.toSet());

            BigDecimal startingPrice = property.getRooms().stream()
                    .map(Room::getPricePerNight)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            int totalGuests = property.getRooms().stream().mapToInt(Room::getMaxGuests).sum();
            int totalRooms = property.getRooms().size();
            int totalBeds = property.getRooms().stream().mapToInt(r -> r.getNumBeds() != null ? r.getNumBeds() : 0).sum();
            int totalBathrooms = property.getRooms().stream().mapToInt(r -> r.getNumBathrooms() != null ? r.getNumBathrooms() : 0).sum();

            return new PropertyCardResponse(
                    property.getId(),
                    property.getName(),
                    property.getSlug(),
                    property.getProvince(),
                    property.getDistrict(),
                    startingPrice,
                    thumbnailUrl,
                    property.getRatingAvg(),
                    property.getRoomCount(),
                    totalGuests,
                    totalRooms,
                    totalBeds,
                    totalBathrooms,
                    new ArrayList<>(allAmenityNames)
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

        List<String> allImageUrls = property.getImages().stream()
                .sorted(Comparator.comparing(PropertyImage::getDisplayOrder))
                .map(PropertyImage::getUrl)
                .collect(Collectors.toCollection(ArrayList::new));

        property.getRooms().forEach(room -> {
            room.getImages().stream()
                    .sorted(Comparator.comparing(RoomImage::getDisplayOrder))
                    .map(RoomImage::getUrl)
                    .forEach(allImageUrls::add);
        });

        Set<AmenityResponse> allAmenities = Stream.concat(
                        property.getAmenities().stream(),
                        property.getRooms().stream().flatMap(room -> room.getAmenities().stream())
                )
                .map(a -> new AmenityResponse(a.getId(), a.getName(), a.getIconName(), a.getType()))
                .collect(Collectors.toSet());

        int totalGuests = property.getRooms().stream().mapToInt(Room::getMaxGuests).sum();
        int totalRooms = property.getRooms().size();
        int totalBeds = property.getRooms().stream().mapToInt(r -> r.getNumBeds() != null ? r.getNumBeds() : 0).sum();
        int totalBathrooms = property.getRooms().stream().mapToInt(r -> r.getNumBathrooms() != null ? r.getNumBathrooms() : 0).sum();

        List<RoomResponse> roomResponses = property.getRooms().stream().
                map(roomMapper::toResponse).toList();

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

                .maxGuests(totalGuests)
                .numBedrooms(totalRooms)
                .numBeds(totalBeds)
                .numBathrooms(totalBathrooms)

                .cleaningFee(property.getCleaningFee())
                .weekendSurchargePercentage(property.getWeekendSurchargePercentage())
                .depositPercentage(property.getDepositPercentage())
                .isPayAtCheckinAllowed(property.getIsPayAtCheckinAllowed())

                .checkInAfter(property.getCheckinAfter())
                .checkInBefore(property.getCheckinBefore())
                .checkOutAfter(property.getCheckoutAfter())
                .checkOutBefore(property.getCheckoutBefore())
                .isInstantBook(property.getIsInstantBook())
                .isSmokingAllowed(property.getIsSmokingAllowed())
                .isPetsAllowed(property.getIsPetsAllowed())
                .isPartyAllowed(property.getIsPartyAllowed())

                .ratingAvg(property.getRatingAvg())
                .reviewCount(property.getReviewCount())

                .categoryName(property.getCategory().getName())
                .rentalTypeName(property.getRentalType().getName())
                .rentalTypeSlug(property.getRentalType().getSlug())

                .host(hostInfo)

                .amenities(new ArrayList<>(allAmenities))
                .imageUrls(allImageUrls)
                .rooms(roomResponses)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveFirstPendingPropertyByHost(Long id) {
        Optional<Property> firstPendingProperty = propertyRepository.findFirstByHostIdAndStatusOrderByCreatedAtAsc(
                id,
                PropertyStatus.PENDING_REVIEW
        );

        if (firstPendingProperty.isPresent()) {
            Property property = firstPendingProperty.get();
            property.setStatus(PropertyStatus.PUBLISHED);
            propertyRepository.save(property);
        }
    }
}
