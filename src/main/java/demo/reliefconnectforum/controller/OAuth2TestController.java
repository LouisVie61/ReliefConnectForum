package demo.reliefconnectforum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Tag(name = "OAuth2 Test", description = "Endpoints for testing OAuth2 integration")
public class OAuth2TestController {

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("/login-with-google")
    @Operation(
        summary = "Get Google OAuth2 Login URL",
        description = "Returns the URL to initiate Google OAuth2 login. Copy this URL and paste it in your browser's address bar.",
        responses = {
            @ApiResponse(responseCode = "200", description = "OAuth2 URL returned successfully",
                content = @Content(schema = @Schema(implementation = Map.class)))
        }
    )
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        String oauth2Url = "http://localhost:" + serverPort + "/oauth2/authorization/google";

        Map<String, String> response = new HashMap<>();
        response.put("message", "Copy and paste this URL in your browser to login with Google");
        response.put("oauth2_url", oauth2Url);
        response.put("instructions", "After successful login, you will receive a JWT token in the response");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2-url")
    @Operation(summary = "Get OAuth2 URL (Deprecated)", deprecated = true)
    public ResponseEntity<String> getOAuth2Url() {
        return ResponseEntity.ok("Please visit: http://localhost:" + serverPort + "/oauth2/authorization/google");
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get authenticated user profile",
        description = "Requires JWT token obtained from OAuth2 login"
    )
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", principal.getAttribute("name"));
        userInfo.put("email", principal.getAttribute("email"));
        userInfo.put("attributes", principal.getAttributes());
        return ResponseEntity.ok(userInfo);
    }
}