package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.ErrorCode;
import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import com.stayhub.backend.Common.Util.UserSubscriptionStatus;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.DTO.Request.RoomCreateRequest;
import com.stayhub.backend.Module.Property.Model.*;
import com.stayhub.backend.Module.Property.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HostDetailRepository hostDetailRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private RentalTypeRepository rentalTypeRepository;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private CancellationPolicyRepository cancellationPolicyRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private User currentUser;
    private HostDetail hostDetail;
    private UserSubscription userSubscription;
    private PropertyCreateRequest request;
    private Category category;
    private RentalType rentalType;
    private CancellationPolicy cancellationPolicy;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);

        hostDetail = new HostDetail();
        hostDetail.setId(1L);
        hostDetail.setUser(currentUser);
        hostDetail.setOnboardingStatus(HostOnboardingStatus.APPROVED);

        userSubscription = new UserSubscription();
        userSubscription.setId(1L);
        userSubscription.setCurrentMaxListings(5);
        userSubscription.setStatus(UserSubscriptionStatus.ACTIVE);

        category = new Category();
        category.setId(1L);
        category.setName("Villa");

        rentalType = new RentalType();
        rentalType.setId(1L);
        rentalType.setSlug("toan-bo-cho-o");

        cancellationPolicy = new CancellationPolicy();
        cancellationPolicy.setId(1L);
        cancellationPolicy.setRefundPercentage(50);

        request = new PropertyCreateRequest(
                1L, 1L, List.of(1L),
                "Province", "District", "Ward", "Detail",
                10.0, 100.0,
                "Beautiful Villa in Vung Tau", "Description...", 1L, true, 50,
                10, BigDecimal.valueOf(100000), 2,
                List.of("url1", "url2", "url3", "url4", "url5"),
                List.of(new RoomCreateRequest("Room 1", "Desc", BigDecimal.valueOf(500000), 2, 1, 1, List.of(1L), List.of("url")))
        );
    }

    @Test
    void createProperty_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(hostDetailRepository.findById(1L)).thenReturn(Optional.of(hostDetail));
        when(userSubscriptionRepository.findFirstByUser_IdAndStatusOrderByStartDateDesc(1L, UserSubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(userSubscription));
        when(propertyRepository.countByHostId(1L)).thenReturn(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(rentalTypeRepository.findById(1L)).thenReturn(Optional.of(rentalType));
        when(cancellationPolicyRepository.findById(1L)).thenReturn(Optional.of(cancellationPolicy));
        when(amenityRepository.findAllById(anySet())).thenReturn(List.of(new Amenity()));

        assertDoesNotThrow(() -> propertyService.createProperty(1L, request));
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void createProperty_HostNotApproved_ThrowsException() {
        hostDetail.setOnboardingStatus(HostOnboardingStatus.REJECTED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(hostDetailRepository.findById(1L)).thenReturn(Optional.of(hostDetail));

        AppException exception = assertThrows(AppException.class, () -> propertyService.createProperty(1L, request));
        assertEquals(ErrorCode.HOST_NOT_APPROVED, exception.getErrorCode());
        verifyNoInteractions(propertyRepository);
    }

    @Test
    void createProperty_ListingLimitReached_ThrowsException() {
        userSubscription.setCurrentMaxListings(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(hostDetailRepository.findById(1L)).thenReturn(Optional.of(hostDetail));
        when(userSubscriptionRepository.findFirstByUser_IdAndStatusOrderByStartDateDesc(1L, UserSubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(userSubscription));
        when(propertyRepository.countByHostId(1L)).thenReturn(2L); // Limit reached

        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> propertyService.createProperty(1L, request));
        assertTrue(exception.getMessage().contains("đạt giới hạn tạo tối đa"));
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_InvalidDeposit_ThrowsException() {
        request = new PropertyCreateRequest(
                1L, 1L, List.of(1L),
                "P", "D", "W", "Dt", 10.0, 100.0, "Name", "Desc", 1L, true, 30, // Deposit 30% but min required is 100 - 50 = 50%
                10, BigDecimal.ONE, 1, List.of("url1", "url2", "url3", "url4", "url5"), List.of(new RoomCreateRequest("Room 1", "Desc", BigDecimal.valueOf(500000), 2, 1, 1, List.of(1L), List.of("url")))
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(hostDetailRepository.findById(1L)).thenReturn(Optional.of(hostDetail));
        when(userSubscriptionRepository.findFirstByUser_IdAndStatusOrderByStartDateDesc(1L, UserSubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(userSubscription));
        when(propertyRepository.countByHostId(1L)).thenReturn(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(rentalTypeRepository.findById(1L)).thenReturn(Optional.of(rentalType));
        when(cancellationPolicyRepository.findById(1L)).thenReturn(Optional.of(cancellationPolicy));

        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> propertyService.createProperty(1L, request));
        assertTrue(exception.getMessage().contains("mức cọc tối thiểu phải là"));
    }
}
