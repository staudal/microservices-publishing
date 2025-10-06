package com.example.article;

import com.example.shared.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public Article create(@RequestBody Article article) {
        log.info("POST /articles - Creating article");
        return articleService.create(article);
    }

    @GetMapping
    public List<Article> readAll() {
        log.info("GET /articles - Fetching all articles");
        return articleService.readAll();
    }

    @GetMapping("/{id}")
    public Article readById(@PathVariable Long id, @RequestParam(required = false) String continent) {
        log.info("GET /articles/{} - continent={}", id, continent);
        return articleService.readById(id, continent);
    }
}
