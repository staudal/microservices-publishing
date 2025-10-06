package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Comment comment) {
        log.info("POST /comments - articleId={}", comment.getArticleId());
        try {
            Comment created = commentService.create(comment);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.warn("POST /comments - Rejected: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public List<Comment> readAll() {
        log.info("GET /comments - Fetching all comments");
        return commentService.readAll();
    }

    @GetMapping("/{id}")
    public Comment readById(@PathVariable Long id) {
        log.info("GET /comments/{}", id);
        return commentService.readById(id);
    }

    @GetMapping("/article/{articleId}")
    public List<Comment> readByArticleId(@PathVariable Long articleId) {
        log.info("GET /comments/article/{}", articleId);
        return commentService.readByArticleId(articleId);
    }

    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}