package com.example.demo;

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

    @Autowired
    private DraftService draftService;

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

        EntityManager em = getEntityManager(continent);
        em.getTransaction().begin();
        em.persist(article);
        em.getTransaction().commit();
        em.close();
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

        EntityManager em = getEntityManager(continent);
        Article article = em.find(Article.class, id);
        em.close();
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
        // Create article from queue message
        Article article = new Article();
        article.setTitle(message.getTitle());
        article.setContinent(message.getContinent());

        create(article);

        // Delete the draft after successful article creation
        draftService.delete(message.getDraftId());

        System.out.println("Article created from draft " + message.getDraftId() + " in continent: " + message.getContinent());
    }
}