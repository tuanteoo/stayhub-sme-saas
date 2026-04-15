package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Service.EmailService;
import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import com.stayhub.backend.Common.Util.SubscriptionTier;
import com.stayhub.backend.Module.Identity.DTO.Request.HostApprovalRequest;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import com.stayhub.backend.Module.Identity.Model.Role;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Model.Profile;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.RoleRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.Model.SubscriptionPlan;
import com.stayhub.backend.Module.Property.Repository.SubscriptionPlanRepository;
import com.stayhub.backend.Module.Property.Repository.UserSubscriptionRepository;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HostOnboardingServiceImplTest {

    @Mock
    private HostDetailRepository hostDetailRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PropertyService propertyService;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private HostOnboardingServiceImpl hostOnboardingService;

    private User user;
    private HostDetail hostDetail;
    private HostApprovalRequest approvalRequest;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile();
        profile.setFullName("John Doe");

        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setProfile(profile);
        user.setRoles(new HashSet<>());

        hostDetail = new HostDetail();
        hostDetail.setId(10L);
        hostDetail.setHostCode("HOU-ABC-DEF-GHI");
        hostDetail.setUser(user);
    }

    @Test
    void reviewHostApplication_HostNotFound_ThrowsException() {
        String hostCode = "HOU-XYZ";
        HostApprovalRequest request = new HostApprovalRequest(HostOnboardingStatus.REJECTED, "Not good");

        when(hostDetailRepository.findByHostCode(hostCode)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> hostOnboardingService.reviewHostApplication(hostCode, request));
        assertEquals("Không tìm thấy hồ sơ đăng ký host!", ex.getMessage());

        verify(hostDetailRepository, never()).save(any());
    }

    @Test
    void reviewHostApplication_StatusRejected_UpdatesStatusAndNote() {
        String hostCode = "HOU-ABC-DEF-GHI";
        HostApprovalRequest request = new HostApprovalRequest(HostOnboardingStatus.REJECTED, "Invalid documents");

        when(hostDetailRepository.findByHostCode(hostCode)).thenReturn(Optional.of(hostDetail));

        hostOnboardingService.reviewHostApplication(hostCode, request);

        assertEquals(HostOnboardingStatus.REJECTED, hostDetail.getOnboardingStatus());
        assertEquals("Invalid documents", hostDetail.getReviewNote());

        verify(hostDetailRepository).save(hostDetail);
        verifyNoInteractions(roleRepository, propertyService, subscriptionPlanRepository, userSubscriptionRepository, emailService);
    }

    @Test
    void reviewHostApplication_StatusRequestChanges_UpdatesStatusAndNote() {
        String hostCode = "HOU-ABC-DEF-GHI";
        HostApprovalRequest request = new HostApprovalRequest(HostOnboardingStatus.REQUEST_CHANGES, "Please update ID front");

        when(hostDetailRepository.findByHostCode(hostCode)).thenReturn(Optional.of(hostDetail));

        hostOnboardingService.reviewHostApplication(hostCode, request);

        assertEquals(HostOnboardingStatus.REQUEST_CHANGES, hostDetail.getOnboardingStatus());
        assertEquals("Please update ID front", hostDetail.getReviewNote());

        verify(hostDetailRepository).save(hostDetail);
        verifyNoInteractions(roleRepository, propertyService, subscriptionPlanRepository, userSubscriptionRepository, emailService);
    }

    @Test
    void reviewHostApplication_StatusApproved_RoleNotFound_ThrowsException() {
        String hostCode = "HOU-ABC-DEF-GHI";
        HostApprovalRequest request = new HostApprovalRequest(HostOnboardingStatus.APPROVED, null);

        when(hostDetailRepository.findByHostCode(hostCode)).thenReturn(Optional.of(hostDetail));
        when(roleRepository.findByName("ROLE_HOST")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> hostOnboardingService.reviewHostApplication(hostCode, request));
        assertEquals("Lỗi hệ thống: Không tìm thấy quyền hợp lệ", ex.getMessage());

        verify(userRepository, never()).save(any());
        verify(hostDetailRepository, never()).save(any());
    }

    @Test
    void reviewHostApplication_StatusApproved_PlanNotFound_ThrowsException() {
        String hostCode = "HOU-ABC-DEF-GHI";
        HostApprovalRequest request = new HostApprovalRequest(HostOnboardingStatus.APPROVED, null);
        Role role = new Role();
        role.setName("ROLE_HOST");

        when(hostDetailRepository.findByHostCode(hostCode)).thenReturn(Optional.of(hostDetail));
        when(roleRepository.findByName("ROLE_HOST")).thenReturn(Optional.of(role));
        when(subscriptionPlanRepository.findByTier(SubscriptionTier.FREE)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> hostOnboardingService.reviewHostApplication(hostCode, request));
        assertEquals("Không tìm thấy gói cước FREE cấu hình trong hệ thống", ex.getMessage());

        verify(userRepository).save(user);
        verify(propertyService).approveFirstPendingPropertyByHost(hostDetail.getId());
        verify(userSubscriptionRepository, never()).save(any());
    }

    @Test
    void reviewHostApplication_StatusApproved_Success() {
        String hostCode = "HOU-ABC-DEF-GHI";
        HostApprovalRequest request = new HostApprovalRequest(HostOnboardingStatus.APPROVED, null);

        Role role = new Role();
        role.setName("ROLE_HOST");

        SubscriptionPlan freePlan = new SubscriptionPlan();
        freePlan.setCommissionRate(java.math.BigDecimal.valueOf(0.05));
        freePlan.setMaxListings(3);
        freePlan.setCreditLimit(java.math.BigDecimal.ZERO);

        when(hostDetailRepository.findByHostCode(hostCode)).thenReturn(Optional.of(hostDetail));
        when(roleRepository.findByName("ROLE_HOST")).thenReturn(Optional.of(role));
        when(subscriptionPlanRepository.findByTier(SubscriptionTier.FREE)).thenReturn(Optional.of(freePlan));

        hostOnboardingService.reviewHostApplication(hostCode, request);

        assertEquals(HostOnboardingStatus.APPROVED, hostDetail.getOnboardingStatus());
        assertNull(hostDetail.getReviewNote());
        assertTrue(user.getRoles().contains(role));

        verify(userRepository).save(user);
        verify(propertyService).approveFirstPendingPropertyByHost(hostDetail.getId());
        verify(userSubscriptionRepository).save(any());
        verify(emailService).sendHostApprovalEmail(eq("john@example.com"), eq("John Doe"));
        verify(hostDetailRepository).save(hostDetail);
    }
}
