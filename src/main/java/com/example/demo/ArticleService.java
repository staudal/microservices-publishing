package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    private DraftService draftService;

    @Autowired
    private ArticleCacheService articleCacheService;

    @Autowired
    private CacheMetricsService cacheMetricsService;

    @Autowired @Qualifier("asiaEntityManager")
    private LocalContainerEntityManagerFactoryBean asiaEmf;

    @Autowired @Qualifier("europeEntityManager")
    private LocalContainerEntityManagerFactoryBean europeEmf;

    @Autowired @Qualifier("africaEntityManager")
    private LocalContainerEntityManagerFactoryBean africaEmf;

    @Autowired @Qualifier("antarcticaEntityManager")
    private LocalContainerEntityManagerFactoryBean antarcticaEmf;

    @Autowired @Qualifier("northAmericaEntityManager")
    private LocalContainerEntityManagerFactoryBean northAmericaEmf;

    @Autowired @Qualifier("oceaniaEntityManager")
    private LocalContainerEntityManagerFactoryBean oceaniaEmf;

    @Autowired @Qualifier("southAmericaEntityManager")
    private LocalContainerEntityManagerFactoryBean southAmericaEmf;

    @Autowired @Qualifier("globalEntityManager")
    private LocalContainerEntityManagerFactoryBean globalEmf;

    public Article create(Article article) {
        String continent = article.getContinent();
        if (continent == null) continent = "GLOBAL";

        log.info("Creating article in {} database: title={}", continent, article.getTitle());

        EntityManager em = getEntityManager(continent);
        em.getTransaction().begin();
        em.persist(article);
        em.getTransaction().commit();
        em.close();

        log.info("Article created successfully: id={}, continent={}", article.getId(), continent);
        return article;
    }

    public List<Article> readAll() {
        List<Article> articles = new ArrayList<>();

        LocalContainerEntityManagerFactoryBean[] allEmfs = {
                asiaEmf, europeEmf, africaEmf, antarcticaEmf,
                northAmericaEmf, oceaniaEmf, southAmericaEmf, globalEmf
        };

        for (LocalContainerEntityManagerFactoryBean emf : allEmfs) {
            EntityManager em = emf.getObject().createEntityManager();
            articles.addAll(em.createQuery("SELECT a FROM Article a", Article.class).getResultList());
            em.close();
        }

        return articles;
    }

    public Article readById(Long id, String continent) {
        if (continent == null) continent = "GLOBAL";

        log.debug("Fetching article: id={}, continent={}", id, continent);

        // Only use cache for GLOBAL articles
        if ("GLOBAL".equalsIgnoreCase(continent)) {
            Article cachedArticle = articleCacheService.getFromCache(id);
            if (cachedArticle != null) {
                log.info("Article cache HIT: id={}", id);
                cacheMetricsService.recordArticleCacheHit();
                return cachedArticle;
            }
            log.info("Article cache MISS: id={}", id);
            cacheMetricsService.recordArticleCacheMiss();
        }

        EntityManager em = getEntityManager(continent);
        Article article = em.find(Article.class, id);
        em.close();

        if (article != null) {
            log.info("Article fetched from database: id={}, continent={}", id, continent);
        } else {
            log.warn("Article not found: id={}, continent={}", id, continent);
        }

        return article;
    }

    private EntityManager getEntityManager(String continent) {
        return switch (continent.toUpperCase()) {
            case "ASIA" -> asiaEmf.getObject().createEntityManager();
            case "EUROPE" -> europeEmf.getObject().createEntityManager();
            case "AFRICA" -> africaEmf.getObject().createEntityManager();
            case "ANTARCTICA" -> antarcticaEmf.getObject().createEntityManager();
            case "NORTH_AMERICA" -> northAmericaEmf.getObject().createEntityManager();
            case "OCEANIA" -> oceaniaEmf.getObject().createEntityManager();
            case "SOUTH_AMERICA" -> southAmericaEmf.getObject().createEntityManager();
            default -> globalEmf.getObject().createEntityManager();
        };
    }

    @RabbitListener(queues = "article-queue")
    public void processArticleQueue(ArticleQueueMessage message) {
        log.info("Received article from queue: draftId={}, continent={}, title={}",
                message.getDraftId(), message.getContinent(), message.getTitle());

        // Create article from queue message
        Article article = new Article();
        article.setTitle(message.getTitle());
        article.setContinent(message.getContinent());

        create(article);

        // Delete the draft after successful article creation
        draftService.delete(message.getDraftId());

        log.info("Article created from draft and draft deleted: draftId={}, articleId={}, continent={}",
                message.getDraftId(), article.getId(), message.getContinent());
    }
}