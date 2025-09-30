package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/publish")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping("/{draftId}")
    public ResponseEntity<String> publishDraft(
            @PathVariable Long draftId,
            @RequestParam String continent) {
        try {
            publisherService.publishDraft(draftId, continent);
            return ResponseEntity.ok("Draft published to article queue");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}