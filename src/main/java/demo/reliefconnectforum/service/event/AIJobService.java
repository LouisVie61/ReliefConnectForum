package demo.reliefconnectforum.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AIJobService {
    private static final String AI_JOB_QUEUE = "ai_jobs_queue";
    private static final Logger logger = LoggerFactory.getLogger(AIJobService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean submitJob(UUID postId) {
        try {
            Map<String, Object> jobPayLoad = Map.of(
                    "postId", postId.toString(),
                    "timestamp", System.currentTimeMillis()
            );

            String jsonPayLoad = objectMapper.writeValueAsString(jobPayLoad);
            redisTemplate.opsForList().rightPush(AI_JOB_QUEUE, jsonPayLoad);
            return true;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize AI job payload for post: {}", postId, e);
            return false;
        } catch (Exception e) {
            logger.error("Failed to submit AI job to Redis for post: {}", postId, e);
            return false;
        }
    }
}
