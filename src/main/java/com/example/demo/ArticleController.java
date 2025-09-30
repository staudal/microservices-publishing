package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public Article create(@RequestBody Article article) {
        return articleService.create(article);
    }

    @GetMapping
    public List<Article> readAll() {
        return articleService.readAll();
    }

    @GetMapping("/{id}")
    public Article readById(@PathVariable Long id, @RequestParam(required = false) String continent) {
        return articleService.readById(id, continent);
    }
}