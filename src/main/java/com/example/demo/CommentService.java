package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RestTemplate restTemplate;
    private static final String PROFANITY_SERVICE_URL = "http://profanity-service:8080/profanity/check";

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
        this.restTemplate = new RestTemplate();
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

    private boolean checkProfanity(String text) {
        try {
            ProfanityCheckRequest request = new ProfanityCheckRequest(text);
            ProfanityCheckResponse response = restTemplate.postForObject(
                    PROFANITY_SERVICE_URL,
                    request,
                    ProfanityCheckResponse.class
            );
            return response != null && response.isProfane();
        } catch (Exception e) {
            // If profanity service is down, allow comment (fail open for availability)
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