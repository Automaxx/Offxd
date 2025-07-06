package com.secureoffice.backend.service;

import com.secureoffice.backend.dto.auth.*;
import com.secureoffice.backend.exception.ResourceNotFoundException;
import com.secureoffice.backend.model.RefreshToken;
import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.RefreshTokenRepository;
import com.secureoffice.backend.repository.UserRepository;
import com.secureoffice.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ActivityLogService activityLogService;

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByUsernameOrEmail(
            loginRequest.getUsernameOrEmail(), 
            loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate refresh token
        String refreshTokenValue = tokenProvider.generateRefreshToken(user.getId());
        
        // Save refresh token to database
        RefreshToken refreshToken = new RefreshToken(
            user, 
            refreshTokenValue, 
            LocalDateTime.now().plusDays(7)
        );
        
        // Remove existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(refreshToken);

        // Log activity
        activityLogService.logActivity(user, "LOGIN", null, null);

        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );

        return new JwtAuthenticationResponse(jwt, refreshTokenValue, userInfo);
    }

    public JwtAuthenticationResponse register(RegisterRequest registerRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            passwordEncoder.encode(registerRequest.getPassword()),
            registerRequest.getFirstName(),
            registerRequest.getLastName()
        );

        User savedUser = userRepository.save(user);

        // Log activity
        activityLogService.logActivity(savedUser, "REGISTER", null, null);

        // Auto-login after registration
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                registerRequest.getUsername(),
                registerRequest.getPassword()
            )
        );

        String jwt = tokenProvider.generateToken(authentication);
        String refreshTokenValue = tokenProvider.generateRefreshToken(savedUser.getId());
        
        RefreshToken refreshToken = new RefreshToken(
            savedUser, 
            refreshTokenValue, 
            LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(refreshToken);

        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getRole().name()
        );

        return new JwtAuthenticationResponse(jwt, refreshTokenValue, userInfo);
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
            .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        
        // Generate new access token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, null
        );
        String newAccessToken = tokenProvider.generateToken(authentication);

        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );

        return new JwtAuthenticationResponse(newAccessToken, requestRefreshToken, userInfo);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
            .ifPresent(token -> {
                activityLogService.logActivity(token.getUser(), "LOGOUT", null, null);
                refreshTokenRepository.delete(token);
            });
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour

        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        // Log activity
        activityLogService.logActivity(user, "PASSWORD_RESET_REQUEST", null, null);
    }

    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);

        userRepository.save(user);

        // Invalidate all refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        // Log activity
        activityLogService.logActivity(user, "PASSWORD_RESET_CONFIRM", null, null);
    }
}
