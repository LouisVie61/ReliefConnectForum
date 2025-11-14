package demo.reliefconnectforum.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

@Service
public class AIJobService {
    private static final String AI_JOB_QUEUE = "ai_jobs_queue";
    private static final Logger logger = LoggerFactory.getLogger(AIJobService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        logger.info("Post created event received for postId: {}", event.getPostId());
        submitJob(event.getPostId());
    }

    public boolean submitJob(UUID postId) {
        try {
            logger.info("Attempting to submit AI job for post: {}", postId);
            Map<String, Object> jobPayLoad = Map.of(
                    "postId", postId.toString(),
                    "timestamp", System.currentTimeMillis()
            );

            Long result = redisTemplate.opsForList().rightPush(AI_JOB_QUEUE, jobPayLoad);
            logger.info("Successfully pushed job to Redis. Queue length: {}", result);
            return true;
        } catch (Exception e) {
            logger.error("Failed to submit AI job to Redis for post: {}", postId, e);
            return false;
        }
    }
}
