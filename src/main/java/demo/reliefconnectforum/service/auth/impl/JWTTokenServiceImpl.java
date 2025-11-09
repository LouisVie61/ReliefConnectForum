package demo.reliefconnectforum.service.auth.impl;

import demo.reliefconnectforum.config.JwtRedisProperties;
import demo.reliefconnectforum.service.auth.JWTTokenService;
import demo.reliefconnectforum.service.event.TokenRevokedEvent;
import demo.reliefconnectforum.service.event.UserAllTokensRevokedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JWTTokenServiceImpl implements JWTTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtRedisProperties properties; // Add this
    private final RedisScript<List> sessionLimitScript;

    public JWTTokenServiceImpl(RedisTemplate<String, Object> redisTemplate,
                               ApplicationEventPublisher eventPublisher,
                               JwtRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.sessionLimitScript = loadLuaScript();
    }

    private RedisScript<List> loadLuaScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("lua/enforce-session-limit.lua")
        ));
        script.setResultType(List.class);
        return script;
    }

    @Override
    public void storeToken(String userId, String token, long durationSeconds) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must not be null or empty");
        }

        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token must not be null or empty");
        }

        int maxSessions = properties.getMaxSessionsPerUser();
        if (maxSessions <= 0) {
            throw new IllegalStateException(
                    "jwt.redis.max-sessions-per-user must be > 0 (current: " + maxSessions + ")"
            );
        }

        // Atomic session limit check (Lua script)
        List<Object> result = redisTemplate.execute(
                sessionLimitScript,
                Collections.singletonList(properties.getUserTokensPrefix() + userId),
                properties.getTokenPrefix(),
                properties.getReverseMapPrefix(),
                String.valueOf(properties.getMaxSessionsPerUser())
        );

        // Step 2: Handle revocation if limit exceeded
        if (result != null && ((Number) result.get(0)).intValue() == 1) {
            String revokedToken = result.get(1).toString();
            log.warn("Session limit reached for user {}. Revoked oldest token: {}",
                    userId, revokedToken);
            eventPublisher.publishEvent(new TokenRevokedEvent(this, userId, revokedToken));
        }

        // Define Redis keys
        String tokenKey = properties.getTokenPrefix() + token;
        String tokenUserKey = properties.getReverseMapPrefix() + token;
        String userTokensKey = properties.getUserTokensPrefix() + userId;

        // Store token (transactional)
        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                // Store token metadata
                operations.opsForHash().put(tokenKey, "userId", userId);
                operations.opsForHash().put(tokenKey, "issuedAt", System.currentTimeMillis());
                operations.expire(tokenKey, durationSeconds, TimeUnit.SECONDS);

                // Store reverse mapping
                operations.opsForValue().set(tokenUserKey, userId, durationSeconds, TimeUnit.SECONDS);

                // Add to user's token set
                operations.opsForSet().add(userTokensKey, token);
                operations.expire(userTokensKey, durationSeconds, TimeUnit.SECONDS);

                operations.exec();
                return null;
            }
        });

        log.info("Stored token for user {} with TTL {}s", userId, durationSeconds);
    }

    /**
     *
     * use this function for stress testing with JMeter
     *
     # Clear Redis
     redis-cli -p 6380 FLUSHDB

     # Rebuild
     mvn clean compile

     # Run with stress test profile
     mvn spring-boot:run -Dspring-boot.run.profiles=stresstest
     *
     *
     * Switch back to dev profile
     * mvn spring-boot:run -Dspring-boot.run.profiles=dev
     *
     @Override
     public void storeToken(String userId, String token, long durationSeconds) {
         if (userId == null || userId.isEmpty()) {
         throw new IllegalArgumentException("userId must not be null or empty");
         }

         if (token == null || token.isEmpty()) {
         throw new IllegalArgumentException("token must not be null or empty");
         }

        // Store token (always executed)
        String tokenKey = properties.getTokenPrefix() + token;
        String tokenUserKey = properties.getReverseMapPrefix() + token;
        String userTokensKey = properties.getUserTokensPrefix() + userId;

        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForHash().put(tokenKey, "userId", userId);
                operations.opsForHash().put(tokenKey, "issuedAt", System.currentTimeMillis());
                operations.expire(tokenKey, durationSeconds, TimeUnit.SECONDS);
                operations.opsForValue().set(tokenUserKey, userId, durationSeconds, TimeUnit.SECONDS);
                operations.opsForSet().add(userTokensKey, token);
                operations.expire(userTokensKey, durationSeconds, TimeUnit.SECONDS);
                operations.exec();
                return null;
            }
        });

        log.info("Stored token for user {} with TTL {}s (throttling disabled)", userId, durationSeconds);
    }
     */

    @Override
    public boolean isTokenValid(String userId, String token) {
        try {
            String tokenKey = properties.getTokenPrefix() + token;
            Object storedUserId = redisTemplate.opsForHash().get(tokenKey, "userId"); // ✅ Changed

            if (storedUserId == null) {
                String userTokensKey = properties.getUserTokensPrefix() + userId;
                redisTemplate.opsForSet().remove(userTokensKey, token);
                return false;
            }

            return MessageDigest.isEqual(
                userId.getBytes(StandardCharsets.UTF_8),
                storedUserId.toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void revokeToken(String userId, String token) {
        asyncRevoke(userId, token);
    }

    @Async
    public void asyncRevoke(String userId, String token) {
        String tokenKey = properties.getTokenPrefix() + token;
        String userTokensKey = properties.getUserTokensPrefix() + userId;

        redisTemplate.delete(tokenKey);
        redisTemplate.opsForSet().remove(userTokensKey, token);

        // Notify other components (e.g., audit, UI)
        eventPublisher.publishEvent(new TokenRevokedEvent(this, userId, token));

        log.info("Revoked token for user {}", userId);
    }

    @Override
    public void revokeAllTokens(String userId) {
        asyncRevokeAll(userId);
    }

    @Async
    public void asyncRevokeAll(String userId) {
        String userTokensKey = properties.getUserTokensPrefix() + userId;

        try {
            Long tokenCount = redisTemplate.opsForSet().size(userTokensKey);

            if (tokenCount != null && tokenCount > 0) {
                // Process in batches of 100
                int batchSize = properties.getRevocationBatchSize();
                for (int i = 0; i < tokenCount; i += batchSize) {
                    List<Object> tokens = redisTemplate.opsForSet().pop(userTokensKey, batchSize);

                    if (tokens != null && !tokens.isEmpty()) {
                        tokens.forEach(token -> {
                            try {
                                redisTemplate.delete(properties.getTokenPrefix() + token);
                            } catch (Exception e) {
                                log.error("Failed to delete token {}: {}", token, e.getMessage());
                            }
                        });
                    }
                }
            }

            redisTemplate.delete(userTokensKey);
            eventPublisher.publishEvent(new UserAllTokensRevokedEvent(this, userId));

            log.info("Revoked all tokens for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to revoke all tokens for user {}: {}", userId, e.getMessage(), e);
        }
    }
}
