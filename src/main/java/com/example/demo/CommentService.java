package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RestTemplate restTemplate;
    private final CommentCacheService commentCacheService;
    private final CacheMetricsService cacheMetricsService;
    private static final String PROFANITY_SERVICE_URL = "http://profanity-service:8080/profanity/check";

    // Circuit breaker state
    private CircuitBreakerState circuitState = CircuitBreakerState.CLOSED;
    private int failureCount = 0;
    private Instant lastFailureTime = null;
    private static final int FAILURE_THRESHOLD = 3;
    private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(30);

    public CommentService(CommentRepository commentRepository,
                         CommentCacheService commentCacheService,
                         CacheMetricsService cacheMetricsService) {
        this.commentRepository = commentRepository;
        this.commentCacheService = commentCacheService;
        this.cacheMetricsService = cacheMetricsService;
        this.restTemplate = new RestTemplate();
    }

    enum CircuitBreakerState {
        CLOSED,  // Normal operation
        OPEN,    // Failing - don't call service
        HALF_OPEN // Testing if service recovered
    }

    public Comment create(Comment comment) {
        // Check for profanity before saving
        boolean hasProfanity = checkProfanity(comment.getText());
        if (hasProfanity) {
            throw new IllegalArgumentException("Comment contains profanity");
        }
        return commentRepository.save(comment);
    }

    public List<Comment> readAll() {
        return commentRepository.findAll();
    }

    public Comment readById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    public List<Comment> readByArticleId(Long articleId) {
        // Use the cache service which handles cache-aside pattern
        return commentCacheService.getCommentsByArticleId(articleId, cacheMetricsService);
    }

    private boolean checkProfanity(String text) {
        // Check circuit breaker state
        if (circuitState == CircuitBreakerState.OPEN) {
            // Check if timeout has passed
            if (lastFailureTime != null &&
                Duration.between(lastFailureTime, Instant.now()).compareTo(TIMEOUT_DURATION) > 0) {
                circuitState = CircuitBreakerState.HALF_OPEN;
                System.out.println("Circuit breaker: OPEN -> HALF_OPEN (testing recovery)");
            } else {
                // Circuit is still open, fail fast
                System.out.println("Circuit breaker: OPEN - skipping profanity check (fail open)");
                return false;
            }
        }

        try {
            ProfanityCheckRequest request = new ProfanityCheckRequest(text);
            ProfanityCheckResponse response = restTemplate.postForObject(
                    PROFANITY_SERVICE_URL,
                    request,
                    ProfanityCheckResponse.class
            );

            // Success - reset circuit breaker
            if (circuitState == CircuitBreakerState.HALF_OPEN) {
                circuitState = CircuitBreakerState.CLOSED;
                failureCount = 0;
                System.out.println("Circuit breaker: HALF_OPEN -> CLOSED (service recovered)");
            }

            return response != null && response.isProfane();
        } catch (Exception e) {
            // Record failure
            failureCount++;
            lastFailureTime = Instant.now();

            System.out.println("Circuit breaker: Profanity service call failed (attempt " + failureCount + "/" + FAILURE_THRESHOLD + ")");

            // Open circuit if threshold reached
            if (failureCount >= FAILURE_THRESHOLD) {
                circuitState = CircuitBreakerState.OPEN;
                System.out.println("Circuit breaker: CLOSED -> OPEN (threshold reached)");
            }

            // Fail open for availability - allow comment through
            return false;
        }
    }

    static class ProfanityCheckRequest {
        private String text;

        public ProfanityCheckRequest(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    static class ProfanityCheckResponse {
        private boolean profane;

        public boolean isProfane() {
            return profane;
        }

        public void setProfane(boolean profane) {
            this.profane = profane;
        }
    }
}