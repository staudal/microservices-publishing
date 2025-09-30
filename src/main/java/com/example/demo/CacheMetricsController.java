package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/metrics")
public class CacheMetricsController {

    @Autowired
    private CacheMetricsService cacheMetricsService;

    @Autowired
    private ArticleCacheService articleCacheService;

    @GetMapping("/cache")
    @ResponseBody
    public Map<String, Object> getCacheMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        CacheMetricsService.CacheMetrics articleMetrics = cacheMetricsService.getArticleCacheMetrics();
        CacheMetricsService.CacheMetrics commentMetrics = cacheMetricsService.getCommentCacheMetrics();

        Map<String, Object> articleCache = new HashMap<>();
        articleCache.put("hits", articleMetrics.getHits());
        articleCache.put("misses", articleMetrics.getMisses());
        articleCache.put("total", articleMetrics.getTotal());
        articleCache.put("hitRatio", articleMetrics.getHitRatio());
        articleCache.put("hitRatioPercentage", articleMetrics.getHitRatioPercentage());

        Map<String, Object> commentCache = new HashMap<>();
        commentCache.put("hits", commentMetrics.getHits());
        commentCache.put("misses", commentMetrics.getMisses());
        commentCache.put("total", commentMetrics.getTotal());
        commentCache.put("hitRatio", commentMetrics.getHitRatio());
        commentCache.put("hitRatioPercentage", commentMetrics.getHitRatioPercentage());

        metrics.put("articleCache", articleCache);
        metrics.put("commentCache", commentCache);

        return metrics;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        CacheMetricsService.CacheMetrics articleMetrics = cacheMetricsService.getArticleCacheMetrics();
        CacheMetricsService.CacheMetrics commentMetrics = cacheMetricsService.getCommentCacheMetrics();

        model.addAttribute("articleMetrics", articleMetrics);
        model.addAttribute("commentMetrics", commentMetrics);

        return "cache-dashboard";
    }

    @GetMapping("/refresh-article-cache")
    @ResponseBody
    public Map<String, String> refreshArticleCache() {
        articleCacheService.refreshCache();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Article cache refresh triggered");
        return response;
    }
}