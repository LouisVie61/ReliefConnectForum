package demo.reliefconnectforum.service.auth;

import org.springframework.stereotype.Service;


public interface JWTTokenService {
    public void storeToken(String userId, String token, long durationSeconds);

    public boolean isTokenValid(String userId, String token);

    public void revokeToken(String userId, String token);

    public void revokeAllTokens(String userId);
}

