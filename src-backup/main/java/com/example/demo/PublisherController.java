package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/publish")
public class PublisherController {

    private static final Logger log = LoggerFactory.getLogger(PublisherController.class);

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping("/{draftId}")
    public ResponseEntity<String> publishDraft(
            @PathVariable Long draftId,
            @RequestParam String continent) {
        log.info("POST /publish/{} - continent={}", draftId, continent);
        try {
            publisherService.publishDraft(draftId, continent);
            return ResponseEntity.ok("Draft published to article queue");
        } catch (IllegalArgumentException e) {
            log.error("POST /publish/{} - Failed: {}", draftId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}