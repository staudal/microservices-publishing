package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftService {

    private final DraftRepository draftRepository;

    public DraftService(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }

    public Draft create(Draft draft) {
        return draftRepository.save(draft);
    }

    public List<Draft> readAll() {
        return draftRepository.findAll();
    }

    public Draft readById(Long id) {
        return draftRepository.findById(id).orElse(null);
    }
}