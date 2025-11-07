package demo.reliefconnectforum.listener;

import demo.reliefconnectforum.config.JwtRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
public class RedisKeyExpirationListener implements MessageListener {

    private final RedisMessageListenerContainer container;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtRedisProperties properties;
    private final int redisDatabase;

    public RedisKeyExpirationListener(
            RedisMessageListenerContainer container,
            RedisTemplate<String, Object> redisTemplate,
            JwtRedisProperties properties,
            @Value("${spring.data.redis.database:0}") int redisDatabase
    ) {
        this.container = container;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.redisDatabase = redisDatabase;
    }

    @PostConstruct
    public void init() {
        String pattern = String.format("__keyevent@%d__:expired", redisDatabase);
        container.addMessageListener(this, new PatternTopic(pattern));
        log.info("Redis key expiration listener initialized for database {}", redisDatabase);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        // Handle token expiration
        if (expiredKey.startsWith(properties.getTokenPrefix())) {
            handleTokenExpiration(expiredKey);
        }
        // Handle reverse mapping expiration
        else if (expiredKey.startsWith("jwt:token-user:")) {
            handleReverseMapExpiration(expiredKey);
        }
    }

    private void handleReverseMapExpiration(String expiredKey) {
        String token = expiredKey.substring("jwt:token-user:".length());

        try {
            String tokenUserKey = "jwt:token-user:" + token;
            Object userIdObj = redisTemplate.opsForValue().get(tokenUserKey);

            if (userIdObj != null) {
                String userId = userIdObj.toString();
                String userTokensKey = properties.getUserTokensPrefix() + userId;

                redisTemplate.opsForSet().remove(userTokensKey, token);
                log.info("[Redis] Cleaned expired token from user {}: {}", userId, token);
            }
        } catch (Exception e) {
            log.error("Error processing reverse map expiration: {}", e.getMessage(), e);
        }
    }

    private void handleTokenExpiration(String expiredKey) {
        // Optional: Add audit logging here
        log.debug("[Redis] Token key expired: {}", expiredKey);
    }
}