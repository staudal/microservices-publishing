package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftService {

    private static final Logger log = LoggerFactory.getLogger(DraftService.class);

    private final DraftRepository draftRepository;

    public DraftService(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }

    public Draft create(Draft draft) {
        log.info("Creating draft: title={}", draft.getTitle());
        Draft saved = draftRepository.save(draft);
        log.info("Draft created: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    public List<Draft> readAll() {
        return draftRepository.findAll();
    }

    public Draft readById(Long id) {
        return draftRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        log.info("Deleting draft: id={}", id);
        draftRepository.deleteById(id);
        log.info("Draft deleted: id={}", id);
    }
}