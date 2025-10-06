package com.example.comment;

import com.example.shared.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

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
                         CacheMetricsService cacheMetricsService,
                         RestTemplate restTemplate) {
        this.commentRepository = commentRepository;
        this.commentCacheService = commentCacheService;
        this.cacheMetricsService = cacheMetricsService;
        this.restTemplate = restTemplate;
    }

    enum CircuitBreakerState {
        CLOSED,  // Normal operation
        OPEN,    // Failing - don't call service
        HALF_OPEN // Testing if service recovered
    }

    public Comment create(Comment comment) {
        log.info("Creating comment for articleId={}", comment.getArticleId());

        // Check for profanity before saving
        boolean hasProfanity = checkProfanity(comment.getText());
        if (hasProfanity) {
            log.warn("Comment rejected due to profanity: articleId={}", comment.getArticleId());
            throw new IllegalArgumentException("Comment contains profanity");
        }

        Comment saved = commentRepository.save(comment);
        log.info("Comment created successfully: id={}, articleId={}", saved.getId(), saved.getArticleId());
        return saved;
    }

    public List<Comment> readAll() {
        return commentRepository.findAll();
    }

    public Comment readById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    public List<Comment> readByArticleId(Long articleId) {
        log.debug("Fetching comments for articleId={}", articleId);
        // Use the cache service which handles cache-aside pattern
        List<Comment> comments = commentCacheService.getCommentsByArticleId(articleId, cacheMetricsService);
        log.info("Fetched {} comments for articleId={}", comments.size(), articleId);
        return comments;
    }

    private boolean checkProfanity(String text) {
        // Check circuit breaker state
        if (circuitState == CircuitBreakerState.OPEN) {
            // Check if timeout has passed
            if (lastFailureTime != null &&
                Duration.between(lastFailureTime, Instant.now()).compareTo(TIMEOUT_DURATION) > 0) {
                circuitState = CircuitBreakerState.HALF_OPEN;
                log.info("Circuit breaker: OPEN -> HALF_OPEN (testing recovery)");
            } else {
                // Circuit is still open, fail fast
                log.warn("Circuit breaker: OPEN - skipping profanity check (fail open)");
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
                log.info("Circuit breaker: HALF_OPEN -> CLOSED (service recovered)");
            }

            boolean isProfane = response != null && response.isProfane();
            log.debug("Profanity check result: isProfane={}", isProfane);
            return isProfane;
        } catch (Exception e) {
            // Record failure
            failureCount++;
            lastFailureTime = Instant.now();

            log.error("Circuit breaker: Profanity service call failed (attempt {}/{}): {}",
                    failureCount, FAILURE_THRESHOLD, e.getMessage());

            // Open circuit if threshold reached
            if (failureCount >= FAILURE_THRESHOLD) {
                circuitState = CircuitBreakerState.OPEN;
                log.error("Circuit breaker: CLOSED -> OPEN (threshold reached)");
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
