package com.example.profanity;

import com.example.shared.model.Profanity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfanityRepository extends JpaRepository<Profanity, Long> {
}
