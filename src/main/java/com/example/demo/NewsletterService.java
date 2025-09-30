package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NewsletterService {

    private final RestTemplate restTemplate;
    private static final String ARTICLE_SERVICE_URL = "http://nginx/articles";

    public NewsletterService() {
        this.restTemplate = new RestTemplate();
    }

    // Run once a day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void sendDailyNewsletter() {
        System.out.println("=== Newsletter Service: Starting daily newsletter at " + LocalDateTime.now() + " ===");

        try {
            // Fetch all articles from article service
            Article[] articles = restTemplate.getForObject(ARTICLE_SERVICE_URL, Article[].class);

            if (articles == null || articles.length == 0) {
                System.out.println("Newsletter: No articles found");
                return;
            }

            // Get the latest article (last in array)
            Article latestArticle = articles[articles.length - 1];

            // Mimic sending email (console log)
            System.out.println("=== SENDING NEWSLETTER EMAIL ===");
            System.out.println("To: subscribers@example.com");
            System.out.println("Subject: Daily Newsletter - Latest Article");
            System.out.println("Body:");
            System.out.println("  Article ID: " + latestArticle.getId());
            System.out.println("  Title: " + latestArticle.getTitle());
            System.out.println("  Continent: " + latestArticle.getContinent());
            System.out.println("================================");
            System.out.println("Newsletter sent successfully!");

        } catch (Exception e) {
            System.err.println("Newsletter: Failed to fetch articles or send newsletter: " + e.getMessage());
        }
    }

    // For testing: run every minute
    // @Scheduled(cron = "0 * * * * *")
    // public void sendDailyNewsletter() { ... }
}