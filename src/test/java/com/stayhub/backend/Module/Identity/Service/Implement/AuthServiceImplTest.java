package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Util.ErrorCode;
import com.stayhub.backend.Common.Util.UserStatus;
import com.stayhub.backend.Common.Util.VerificationType;
import com.stayhub.backend.Module.Identity.DTO.Request.RegisterGuestRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.LoginRequest;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;
import com.stayhub.backend.Module.Identity.Model.Profile;
import com.stayhub.backend.Module.Identity.Model.RefreshToken;
import com.stayhub.backend.Module.Identity.Model.Role;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Model.VerificationToken;
import com.stayhub.backend.Module.Identity.Repository.ProfileRepository;
import com.stayhub.backend.Module.Identity.Repository.RoleRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Identity.Repository.VerificationTokenRepository;
import com.stayhub.backend.Module.Identity.Repository.RefreshTokenRepository;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import com.stayhub.backend.Module.Identity.Security.JwtTokenProvider;
import com.stayhub.backend.Common.Service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterGuestRequest request;
    private User newUnverifiedUser;
    private MockHttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        request = new RegisterGuestRequest("Test Name", "test@email.com", "password");
        httpRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerGuest_NewUser_Success() {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .email(request.email())
                .status(UserStatus.UNVERIFIED)
                .build();
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String result = authService.registerGuest(request);

        assertEquals("Đăng ký tài khoản thành công. Vui lòng kiểm tra email để xác thực.", result);

        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmailAsync(eq(request.email()), eq(request.fullName()), anyString());
    }

    @Test
    void registerGuest_ExistingUnverifiedUser_Success() {
        User existingUser = User.builder()
                .email(request.email())
                .status(UserStatus.UNVERIFIED)
                .build();
        existingUser.setId(1L);

        Profile existingProfile = new Profile();
        existingProfile.setFullName("Old Name");
        existingProfile.setUser(existingUser);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword2");
        when(profileRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingProfile));

        String result = authService.registerGuest(request);

        assertEquals("Đăng ký tài khoản thành công. Vui lòng kiểm tra email để xác thực.", result);

        verify(userRepository).save(existingUser);
        assertEquals("encodedPassword2", existingUser.getPassword());

        verify(profileRepository).save(existingProfile);
        assertEquals("Test Name", existingProfile.getFullName());

        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmailAsync(eq(request.email()), eq(request.fullName()), anyString());

        verifyNoInteractions(roleRepository);
    }

    @Test
    void registerGuest_EmailExistsAndActive_ThrowsException() {
        User existingUser = User.builder()
                .email(request.email())
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));

        AppException exception = assertThrows(AppException.class, () -> authService.registerGuest(request));
        assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode());

        verify(userRepository, never()).save(any());
        verifyNoInteractions(profileRepository, roleRepository, tokenRepository, emailService);
    }

    @Test
    void registerGuest_ExistingUnverifiedUser_ProfileNotFound_ThrowsException() {
        User existingUser = User.builder()
                .email(request.email())
                .status(UserStatus.UNVERIFIED)
                .build();
        existingUser.setId(1L);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(profileRepository.findById(existingUser.getId())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> authService.registerGuest(request));
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());

        verify(profileRepository, never()).save(any());
        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void registerGuest_NewUser_RoleNotFound_ThrowsException() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> authService.registerGuest(request));
        assertEquals(ErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());

        verify(userRepository, never()).save(any());
        verifyNoInteractions(profileRepository, tokenRepository, emailService);
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("test@email.com", "password");
        httpRequest.setRemoteAddr("127.0.0.1");
        httpRequest.addHeader("User-Agent", "Mozilla/5.0");

        Role role = new Role();
        role.setName("ROLE_USER");

        User user = User.builder()
                .email("test@email.com")
                .status(UserStatus.ACTIVE)
                .roles(java.util.Set.of(role))
                .build();
        user.setId(1L);

        Profile profile = new Profile();
        profile.setFullName("Test User");
        profile.setAvatarUrl("http://avatar.url");

        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(jwtTokenProvider.generateAccessToken(auth)).thenReturn("mockAccessToken");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.accessToken());
        assertNotNull(response.refreshToken());
        assertFalse(response.roles().isEmpty());
        assertTrue(response.roles().contains("ROLE_USER"));
        assertEquals("ACTIVE", response.status());
        assertEquals("Test User", response.userInfResponse().fullName());
        assertEquals("http://avatar.url", response.userInfResponse().avatarUrl());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(userRepository).save(user);
    }

    @Test
    void login_BadCredentials_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("test@email.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verifyNoInteractions(profileRepository, jwtTokenProvider, refreshTokenRepository, userRepository);
    }

    @Test
    void login_ProfileNotFound_SuccessWithEmptyProfile() {
        LoginRequest loginRequest = new LoginRequest("test@email.com", "password");

        User user = User.builder()
                .email("test@email.com")
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(1L);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty()); // No Profile
        when(jwtTokenProvider.generateAccessToken(auth)).thenReturn("mockAccessToken");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNull(response.userInfResponse().fullName()); // Because empty profile defaults to null
        assertNull(response.userInfResponse().avatarUrl());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(userRepository).save(user);
    }
}
