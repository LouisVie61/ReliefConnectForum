package demo.reliefconnectforum.config;

import demo.reliefconnectforum.repository.UserRepository;
import demo.reliefconnectforum.service.auth.JWTTokenService;
import demo.reliefconnectforum.service.auth.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JWTTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String jwtToken = null;

        if (header != null && header.startsWith("Bearer ")) {
            jwtToken = header.substring(7);
        } else if (request.getCookies() != null) {
            jwtToken = Arrays.stream(request.getCookies())
                    .filter(c -> "ACCESS_TOKEN".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (jwtToken == null) {
            chain.doFilter(request, response);
            return;
        }

        String username = null;
        try {
            username = jwtUtil.extractUsername(jwtToken);
        } catch (ExpiredJwtException e) {
            logger.debug("JWT expired");
        } catch (Exception e) {
            logger.debug("JWT parse error: " + e.getMessage());
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean signatureValid = jwtUtil.isTokenValid(jwtToken, userDetails);
            boolean redisValid = false;

            if (signatureValid) {
                var userEntity = userRepository.findByEmail(username);
                if (userEntity != null) {
                    redisValid = jwtTokenService.isTokenValid(userEntity.getId().toString(), jwtToken);
                }
            }

            if (signatureValid && redisValid) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
    private boolean isPublic(String path) {
        return path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/swagger-ui.html") ||
                path.startsWith("/v3/api-docs") ||  // Changed: removed trailing slash
                path.equals("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars/") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/h2-console/") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/") ||
                path.startsWith("/test/");
    }
}