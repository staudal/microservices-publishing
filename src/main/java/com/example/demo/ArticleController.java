package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleRepository articleRepository;

    public ArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @PostMapping
    public Article create(@RequestBody Article article) {
        return articleRepository.save(article);
    }

    @GetMapping
    public List<Article> readAll() {
        return articleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Article readById(@PathVariable Long id) {
        return articleRepository.findById(id).orElse(null);
    }
}