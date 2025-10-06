package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfanityService {

    private static final Logger log = LoggerFactory.getLogger(ProfanityService.class);

    private final ProfanityRepository profanityRepository;

    public ProfanityService(ProfanityRepository profanityRepository) {
        this.profanityRepository = profanityRepository;
    }

    public boolean checkProfanity(String text) {
        log.debug("Checking text for profanity");

        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        // Check against database
        List<Profanity> dbProfanities = profanityRepository.findAll();
        for (Profanity profanity : dbProfanities) {
            if (lowerText.contains(profanity.getWord().toLowerCase())) {
                log.warn("Profanity detected: word={}", profanity.getWord());
                return true;
            }
        }

        log.debug("No profanity detected");
        return false;
    }

    public Profanity addProfanity(Profanity profanity) {
        return profanityRepository.save(profanity);
    }

    public List<Profanity> getAllProfanities() {
        return profanityRepository.findAll();
    }
}