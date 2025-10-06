package com.example.profanity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.example.shared.model")
public class ProfanityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfanityApplication.class, args);
    }
}
