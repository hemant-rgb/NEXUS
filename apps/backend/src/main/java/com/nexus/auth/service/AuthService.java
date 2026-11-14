package com.nexus.auth.service;

import com.nexus.auth.dto.LoginRequest;
import com.nexus.auth.dto.RegisterRequest;
import com.nexus.common.exception.DuplicateResourceException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.common.security.JwtService;
import com.nexus.common.security.UserPrincipal;
import com.nexus.profile.service.ProfileService;
import com.nexus.user.dto.UserResponse;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ProfileService profileService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            ProfileService profileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.profileService = profileService;
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.fullName().trim()
        );

        User savedUser = userRepository.save(user);

        // Initialize empty profile and preferences
        profileService.getOrCreateProfile(savedUser.getId());
        profileService.getOrCreatePreferences(savedUser.getId());

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        ResponseCookie cookie = jwtService.createSessionCookie(token);

        UserResponse userResponse = mapToUserResponse(savedUser);
        return new AuthResult(userResponse, cookie);
    }

    @Transactional(readOnly = true)
    public AuthResult login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        ResponseCookie cookie = jwtService.createSessionCookie(token);

        UserResponse userResponse = mapToUserResponse(user);
        return new AuthResult(userResponse, cookie);
    }

    public ResponseCookie logout() {
        return jwtService.createCleanSessionCookie();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }

    public record AuthResult(UserResponse user, ResponseCookie cookie) {
        public HttpHeaders createHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
            return headers;
        }
    }
}
