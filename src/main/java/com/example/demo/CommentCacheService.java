package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CommentCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CommentRepository commentRepository;

    private static final String CACHE_KEY_PREFIX = "comments:article:";
    private static final int MAX_CACHED_ARTICLES = 30;
    private static final int CACHE_TTL_DAYS = 1;

    // LRU tracking using LinkedHashMap with access order
    private final Map<Long, Boolean> lruTracker = new LinkedHashMap<Long, Boolean>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
            if (size() > MAX_CACHED_ARTICLES) {
                // Remove from Redis when evicted from LRU
                String key = CACHE_KEY_PREFIX + eldest.getKey();
                redisTemplate.delete(key);
                System.out.println("CommentCache: Evicted article " + eldest.getKey() + " (LRU)");
                return true;
            }
            return false;
        }
    };

    public List<Comment> getCommentsByArticleId(Long articleId, CacheMetricsService metricsService) {
        String key = CACHE_KEY_PREFIX + articleId;

        // Try to get from cache
        List<Comment> cachedComments = (List<Comment>) redisTemplate.opsForValue().get(key);

        if (cachedComments != null) {
            // Cache hit - record metric and update LRU
            metricsService.recordCommentCacheHit();
            synchronized (lruTracker) {
                lruTracker.put(articleId, true);
            }
            return cachedComments;
        }

        // Cache miss - record metric
        metricsService.recordCommentCacheMiss();

        // Fetch from database
        List<Comment> comments = commentRepository.findByArticleId(articleId);

        // Store in cache
        redisTemplate.opsForValue().set(key, comments, CACHE_TTL_DAYS, TimeUnit.DAYS);

        // Update LRU (this will trigger eviction if needed)
        synchronized (lruTracker) {
            lruTracker.put(articleId, true);
        }

        return comments;
    }

    public void invalidateCache(Long articleId) {
        String key = CACHE_KEY_PREFIX + articleId;
        redisTemplate.delete(key);
        synchronized (lruTracker) {
            lruTracker.remove(articleId);
        }
        System.out.println("CommentCache: Invalidated cache for article " + articleId);
    }

    public int getCachedArticleCount() {
        synchronized (lruTracker) {
            return lruTracker.size();
        }
    }
}