package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import com.stayhub.backend.Module.Identity.DTO.Request.HostRegistrationWithPropertyRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.RoleRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HostOnboardingServiceImpl Tests")
class HostOnboardingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HostDetailRepository hostDetailRepository;

    @Mock
    private PropertyService propertyService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private HostOnboardingServiceImpl hostOnboardingService;

    private String testEmail;
    private User testUser;
    private HostVerificationRequest hostVerificationRequest;
    private PropertyCreateRequest propertyCreateRequest;
    private HostRegistrationWithPropertyRequest hostRegistrationWithPropertyRequest;

    @BeforeEach
    void setUp() {
        testEmail = "host@example.com";

        // Create test user
        testUser = User.builder()
                .email(testEmail)
                .password("hashedPassword")
                .roles(new HashSet<>())
                .build();
        testUser.setId(1L); // Set id directly since it's inherited from AbstractEntity

        // Create test host verification request
        hostVerificationRequest = new HostVerificationRequest(
                "0123456789",
                "support@example.com",
                "123456789",
                "https://example.com/front.jpg",
                "https://example.com/back.jpg",
                "BL123456",
                "https://example.com/license.jpg"
        );

        // Create test property create request with all required parameters
        propertyCreateRequest = new PropertyCreateRequest(
                1L,                                    // rentalTypeId
                1L,                                    // categoryId
                List.of(1L, 2L, 3L),                   // amenityIds
                "City",                                // province
                "District",                            // district
                "Ward",                                // ward
                "123 Test Street",                     // addressDetail
                10.7769,                               // latitude
                106.7009,                              // longitude
                "Beautiful Test Property",             // name
                "A test property for unit testing",    // description
                10,                                    // weekendSurchargePercentage
                new java.math.BigDecimal("50.00"),    // cleaningFee
                List.of("https://example.com/img1.jpg",
                        "https://example.com/img2.jpg",
                        "https://example.com/img3.jpg",
                        "https://example.com/img4.jpg",
                        "https://example.com/img5.jpg"),  // imageUrls
                List.of()                              // rooms (empty for now)
        );

        // Create test host registration request
        hostRegistrationWithPropertyRequest = new HostRegistrationWithPropertyRequest(
                hostVerificationRequest,
                propertyCreateRequest
        );
    }

    @Test
    @DisplayName("Should successfully submit host application with new HostDetail and generate unique hostCode")
    void testSubmitHostApplication_HappyPath_NewHostDetail() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(hostDetailRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        when(hostDetailRepository.existsByHostCode(any())).thenReturn(false);
        when(hostDetailRepository.save(any(HostDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        hostOnboardingService.submitHostApplication(testEmail, hostRegistrationWithPropertyRequest);

        // Assert
        // Verify that userRepository.findByEmail was called with correct email
        verify(userRepository, times(1)).findByEmail(testEmail);

        // Verify that hostDetailRepository.findById was called with correct user id
        verify(hostDetailRepository, times(1)).findById(testUser.getId());

        // Capture the HostDetail object passed to save method
        ArgumentCaptor<HostDetail> hostDetailCaptor = ArgumentCaptor.forClass(HostDetail.class);
        verify(hostDetailRepository, times(1)).save(hostDetailCaptor.capture());

        HostDetail savedHostDetail = hostDetailCaptor.getValue();

        // Verify that hostCode is generated (not null and follows the pattern)
        assertNotNull(savedHostDetail.getHostCode(), "Host code should not be null");
        assertTrue(savedHostDetail.getHostCode().matches("HOU-[A-Z0-9]{3}-[A-Z0-9]{3}-[A-Z0-9]{3}"),
                "Host code should follow the pattern HOU-XXX-XXX-XXX");

        // Verify that onboarding status is set to PENDING_REVIEW
        assertEquals(HostOnboardingStatus.PENDING_REVIEW, savedHostDetail.getOnboardingStatus(),
                "Onboarding status should be PENDING_REVIEW");

        // Verify that user is correctly associated
        assertEquals(testUser, savedHostDetail.getUser(), "Host detail should be associated with the correct user");

        // Verify that all host details are correctly set
        assertEquals(hostVerificationRequest.businessPhone(), savedHostDetail.getBusinessPhone());
        assertEquals(hostVerificationRequest.supportEmail(), savedHostDetail.getSupportEmail());
        assertEquals(hostVerificationRequest.identityCardNumber(), savedHostDetail.getIdentityCardNumber());
        assertEquals(hostVerificationRequest.identityCardFrontUrl(), savedHostDetail.getIdentityCardFrontUrl());
        assertEquals(hostVerificationRequest.identityCardBackUrl(), savedHostDetail.getIdentityCardBackUrl());
        assertEquals(hostVerificationRequest.businessLicenseNumber(), savedHostDetail.getBusinessLicenseNumber());
        assertEquals(hostVerificationRequest.businessLicenseUrl(), savedHostDetail.getBusinessLicenseUrl());

        // Verify that propertyService.createProperty was called exactly once with correct parameters
        verify(propertyService, times(1)).createProperty(testEmail, propertyCreateRequest);

        // Verify no other unexpected calls
        verifyNoMoreInteractions(userRepository, hostDetailRepository, propertyService);
    }

    @Test
    @DisplayName("Should use existing hostCode if HostDetail already exists")
    void testSubmitHostApplication_ExistingHostDetail_PreservesHostCode() {
        // Arrange
        String existingHostCode = "HOU-ABC-DEF-GHI";
        HostDetail existingHostDetail = HostDetail.builder()
                .user(testUser)
                .hostCode(existingHostCode)
                .build();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(hostDetailRepository.findById(testUser.getId())).thenReturn(Optional.of(existingHostDetail));
        when(hostDetailRepository.save(any(HostDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        hostOnboardingService.submitHostApplication(testEmail, hostRegistrationWithPropertyRequest);

        // Assert
        ArgumentCaptor<HostDetail> hostDetailCaptor = ArgumentCaptor.forClass(HostDetail.class);
        verify(hostDetailRepository, times(1)).save(hostDetailCaptor.capture());

        HostDetail savedHostDetail = hostDetailCaptor.getValue();

        // Verify that existing hostCode is preserved
        assertEquals(existingHostCode, savedHostDetail.getHostCode(),
                "Existing host code should be preserved");

        // Verify that propertyService.createProperty was called exactly once
        verify(propertyService, times(1)).createProperty(testEmail, propertyCreateRequest);
    }

    @Test
    @DisplayName("Should generate unique hostCode on each attempt if collision detected")
    void testSubmitHostApplication_GeneratesUniqueHostCode_OnCollisionDetected() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(hostDetailRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // First call returns true (collision), subsequent calls return false
        when(hostDetailRepository.existsByHostCode(any()))
                .thenReturn(true, false);

        when(hostDetailRepository.save(any(HostDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        hostOnboardingService.submitHostApplication(testEmail, hostRegistrationWithPropertyRequest);

        // Assert
        ArgumentCaptor<HostDetail> hostDetailCaptor = ArgumentCaptor.forClass(HostDetail.class);
        verify(hostDetailRepository, times(1)).save(hostDetailCaptor.capture());

        HostDetail savedHostDetail = hostDetailCaptor.getValue();

        // Verify that a unique hostCode was generated
        assertNotNull(savedHostDetail.getHostCode());
        assertTrue(savedHostDetail.getHostCode().matches("HOU-[A-Z0-9]{3}-[A-Z0-9]{3}-[A-Z0-9]{3}"));

        // Verify that existsByHostCode was called more than once (due to collision)
        verify(hostDetailRepository, atLeast(2)).existsByHostCode(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found by email")
    void testSubmitHostApplication_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> hostOnboardingService.submitHostApplication(nonExistentEmail, hostRegistrationWithPropertyRequest),
                "Should throw ResourceNotFoundException when user is not found"
        );

        // Verify the exact exception message
        assertEquals("Không tìm thấy tài khoản người dùng!", exception.getMessage(),
                "Exception message should match exactly");

        // Verify that userRepository.findByEmail was called with correct email
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);

        // Verify that NO other interactions occurred with the repositories and services
        verifyNoInteractions(hostDetailRepository, propertyService, roleRepository);

        // Additional verification: ensure these methods were never called
        verify(hostDetailRepository, never()).findById(any());
        verify(hostDetailRepository, never()).save(any(HostDetail.class));
        verify(propertyService, never()).createProperty(any(), any());
    }
}

