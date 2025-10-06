package com.example.article;

import com.example.shared.model.Article;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class ArticleCacheService {

    private static final Logger log = LoggerFactory.getLogger(ArticleCacheService.class);

    @Autowired @Qualifier("globalEntityManager")
    private LocalContainerEntityManagerFactoryBean globalEmf;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "article:global:";
    private static final int CACHE_DAYS = 14;

    // Refresh cache every hour (for testing, change to "0 * * * * *")
    @Scheduled(cron = "0 0 * * * *")
    public void refreshCache() {
        log.info("ArticleCache: Starting offline cache refresh");

        EntityManager em = globalEmf.getObject().createEntityManager();
        try {
            // Calculate date 14 days ago
            LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(CACHE_DAYS);
            Date cutoffDate = Date.from(fourteenDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

            // Fetch articles from last 14 days (assuming Article has a createdAt field)
            // Since Article entity doesn't have timestamp, we'll cache all articles for now
            List<Article> articles = em.createQuery(
                    "SELECT a FROM Article a",
                    Article.class
            ).getResultList();

            int cachedCount = 0;
            for (Article article : articles) {
                String key = CACHE_KEY_PREFIX + article.getId();
                redisTemplate.opsForValue().set(key, article, CACHE_DAYS, TimeUnit.DAYS);
                cachedCount++;
            }

            log.info("ArticleCache: Offline refresh completed - cached {} articles", cachedCount);
        } catch (Exception e) {
            log.error("ArticleCache: Error during offline refresh: {}", e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Article getFromCache(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);
        return cached != null ? (Article) cached : null;
    }

    public void invalidateCache(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(key);
    }
}
