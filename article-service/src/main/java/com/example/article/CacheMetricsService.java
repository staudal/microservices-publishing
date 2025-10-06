package com.example.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheMetricsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_CACHE_HITS = "metrics:article:hits";
    private static final String ARTICLE_CACHE_MISSES = "metrics:article:misses";

    public void recordArticleCacheHit() {
        redisTemplate.opsForValue().increment(ARTICLE_CACHE_HITS);
    }

    public void recordArticleCacheMiss() {
        redisTemplate.opsForValue().increment(ARTICLE_CACHE_MISSES);
    }

    public CacheMetrics getArticleCacheMetrics() {
        Long hits = getLongValue(ARTICLE_CACHE_HITS);
        Long misses = getLongValue(ARTICLE_CACHE_MISSES);
        return new CacheMetrics(hits, misses);
    }

    private Long getLongValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return 0L;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Long) return (Long) value;
        return Long.parseLong(value.toString());
    }

    public static class CacheMetrics {
        private final long hits;
        private final long misses;
        private final long total;
        private final double hitRatio;

        public CacheMetrics(long hits, long misses) {
            this.hits = hits;
            this.misses = misses;
            this.total = hits + misses;
            this.hitRatio = total > 0 ? (double) hits / total : 0.0;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getTotal() {
            return total;
        }

        public double getHitRatio() {
            return hitRatio;
        }

        public String getHitRatioPercentage() {
            return String.format("%.2f%%", hitRatio * 100);
        }
    }
}
