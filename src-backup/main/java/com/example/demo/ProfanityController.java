package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profanity")
public class ProfanityController {

    private final ProfanityService profanityService;

    public ProfanityController(ProfanityService profanityService) {
        this.profanityService = profanityService;
    }

    @PostMapping("/check")
    public ProfanityCheckResponse check(@RequestBody ProfanityCheckRequest request) {
        boolean isProfane = profanityService.checkProfanity(request.getText());
        return new ProfanityCheckResponse(isProfane);
    }

    @PostMapping
    public Profanity add(@RequestBody Profanity profanity) {
        return profanityService.addProfanity(profanity);
    }

    @GetMapping
    public List<Profanity> getAll() {
        return profanityService.getAllProfanities();
    }

    static class ProfanityCheckRequest {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    static class ProfanityCheckResponse {
        private boolean profane;

        public ProfanityCheckResponse(boolean profane) {
            this.profane = profane;
        }

        public boolean isProfane() {
            return profane;
        }

        public void setProfane(boolean profane) {
            this.profane = profane;
        }
    }
}