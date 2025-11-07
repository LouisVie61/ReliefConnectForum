package demo.reliefconnectforum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt.redis")
@Data
public class JwtRedisProperties {
    private String tokenPrefix = "jwt:token:";
    private String userTokensPrefix = "jwt:user:";
    private String reverseMapPrefix = "jwt:token-user:";
    private int revocationBatchSize = 1000;
    private int maxSessionsPerUser = 5;
}
