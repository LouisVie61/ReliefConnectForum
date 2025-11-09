package demo.reliefconnectforum.service.auth.impl;

import demo.reliefconnectforum.Enum.UserRoleEnum;
import demo.reliefconnectforum.config.JwtUtil;
import demo.reliefconnectforum.dto.request.UserLoginRequest;
import demo.reliefconnectforum.dto.request.UserRegisterRequest;
import demo.reliefconnectforum.dto.response.UserLoginResponse;
import demo.reliefconnectforum.dto.response.UserRegisterResponse;
import demo.reliefconnectforum.entity.User;
import demo.reliefconnectforum.repository.UserRepository;
import demo.reliefconnectforum.service.auth.AuthService;
import demo.reliefconnectforum.service.auth.JWTTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JWTTokenService jwtTokenService;

    @Override
    public UserRegisterResponse register(UserRegisterRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRoleEnum.USER);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return UserRegisterResponse.builder()
                .id(UUID.randomUUID())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .phoneNumber(savedUser.getPhone())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        long accessTtl = jwtUtil.getAccessTokenExpiration() / 1000;
        long refreshTtl = jwtUtil.getRefreshTokenExpiration() / 1000;

        jwtTokenService.storeToken(user.getId().toString(), accessToken, accessTtl);
        jwtTokenService.storeToken(user.getId().toString(), refreshToken, refreshTtl);


        return UserLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserLoginResponse refreshToken(String refreshToken) {
        try {
            String email = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            jwtTokenService.revokeToken(user.getId().toString(), refreshToken);

            if (jwtUtil.isTokenValid(refreshToken, email)) {
                String newAccessToken = jwtUtil.generateAccessToken(email);
                String newRefreshToken = jwtUtil.generateRefreshToken(email);

                return UserLoginResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType("Bearer")
                        .expiresIn(jwtUtil.getAccessTokenExpiration())
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build();
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    @Override
    public void logout(String token) {
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        jwtTokenService.revokeToken(user.getId().toString(), token);
    }
}