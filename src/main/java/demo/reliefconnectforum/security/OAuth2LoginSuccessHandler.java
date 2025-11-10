package demo.reliefconnectforum.security;

import demo.reliefconnectforum.config.JwtUtil;
import demo.reliefconnectforum.entity.User;
import demo.reliefconnectforum.repository.UserRepository;
import demo.reliefconnectforum.service.auth.JWTTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final JWTTokenService jwtTokenService;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil,
                                     UserRepository userRepository,
                                     JWTTokenService jwtTokenService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not provided by OAuth2 provider");
            return;
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        String access = jwtUtil.generateAccessToken(email);
        String refresh = jwtUtil.generateRefreshToken(email);

        long accessTtl = jwtUtil.getAccessTokenExpiration() / 1000;
        long refreshTtl = jwtUtil.getRefreshTokenExpiration() / 1000;

        jwtTokenService.storeToken(user.getId().toString(), access, accessTtl);
        jwtTokenService.storeToken(user.getId().toString(), refresh, refreshTtl);

        addCookie(response, "ACCESS_TOKEN", access, (int) accessTtl);
        addCookie(response, "REFRESH_TOKEN", refresh, (int) refreshTtl);

        log.info("OAuth2 login successful for user: {}", email);

        String redirectUrl = String.format(
            "http://localhost:8080/swagger-ui/index.html?token=%s&email=%s",
            access, email
        );

        response.sendRedirect(redirectUrl);
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}