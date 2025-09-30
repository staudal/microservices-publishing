package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfanityService {

    private final ProfanityRepository profanityRepository;

    public ProfanityService(ProfanityRepository profanityRepository) {
        this.profanityRepository = profanityRepository;
    }

    public boolean checkProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        // Check against database
        List<Profanity> dbProfanities = profanityRepository.findAll();
        for (Profanity profanity : dbProfanities) {
            if (lowerText.contains(profanity.getWord().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public Profanity addProfanity(Profanity profanity) {
        return profanityRepository.save(profanity);
    }

    public List<Profanity> getAllProfanities() {
        return profanityRepository.findAll();
    }
}