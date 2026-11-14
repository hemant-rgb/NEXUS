package com.nexus.auth;

import com.nexus.auth.dto.LoginRequest;
import com.nexus.auth.dto.RegisterRequest;
import com.nexus.auth.service.AuthService;
import com.nexus.common.exception.DuplicateResourceException;
import com.nexus.common.security.JwtService;
import com.nexus.common.security.UserPrincipal;
import com.nexus.profile.service.ProfileService;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ProfileService profileService;

    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService(TEST_SECRET, 86400000L, "nexus_session");
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                profileService
        );
    }

    @Test
    @DisplayName("Should successfully register a new user and return session cookie")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("dev@nexus.io", "password123", "Nexus Developer");

        when(userRepository.existsByEmail("dev@nexus.io")).thenReturn(false);

        User savedUser = new User("dev@nexus.io", passwordEncoder.encode("password123"), "Nexus Developer");
        UUID userId = UUID.randomUUID();
        savedUser.setId(userId);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthService.AuthResult result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.user().email()).isEqualTo("dev@nexus.io");
        assertThat(result.user().fullName()).isEqualTo("Nexus Developer");
        assertThat(result.cookie().getValue()).isNotEmpty();
        assertThat(result.cookie().getName()).isEqualTo("nexus_session");

        verify(profileService).getOrCreateProfile(userId);
        verify(profileService).getOrCreatePreferences(userId);
    }

    @Test
    @DisplayName("Should reject registration when email already exists")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("existing@nexus.io", "password123", "Existing User");

        when(userRepository.existsByEmail("existing@nexus.io")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("dev@nexus.io", "password123");
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "dev@nexus.io", "hashedPassword", "ROLE_USER");
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);

        User user = new User("dev@nexus.io", "hashedPassword", "Nexus Developer");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AuthService.AuthResult result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.user().email()).isEqualTo("dev@nexus.io");
        assertThat(result.cookie().getValue()).isNotEmpty();
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid login")
    void testLoginBadCredentials() {
        LoginRequest request = new LoginRequest("dev@nexus.io", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Should return clean session cookie on logout")
    void testLogout() {
        ResponseCookie cookie = authService.logout();

        assertThat(cookie.getMaxAge().getSeconds()).isZero();
        assertThat(cookie.getValue()).isEmpty();
    }
}
