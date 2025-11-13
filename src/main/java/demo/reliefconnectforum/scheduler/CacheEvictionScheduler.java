package demo.reliefconnectforum.scheduler;

import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.repository.PostRepository;
import org.springframework.cache.Cache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CacheEvictionScheduler {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedDelay = 10000) // run every 10 seconds
    public void evictStaleCache() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(15);
        List<Post> recentlyUpdatedPosts = postRepository.findRecentlyUpdated(threshold);

        if (recentlyUpdatedPosts.isEmpty()) {
            return;
        }

        Cache postByIdCache = cacheManager.getCache("postById");
        for (Post post : recentlyUpdatedPosts) {
            if (postByIdCache != null) {
                postByIdCache.evict(post.getId());
            }
        }

        if (recentlyUpdatedPosts.size() > 5) {
            evictCacheSafely("allPosts");
            evictCacheSafely("postsByPlace");
            evictCacheSafely("postsByPlaces");
        }
    }

    private void evictCacheSafely(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
