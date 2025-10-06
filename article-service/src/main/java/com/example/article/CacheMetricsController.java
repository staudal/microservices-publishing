package com.example.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
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

        Map<String, Object> articleCache = new HashMap<>();
        articleCache.put("hits", articleMetrics.getHits());
        articleCache.put("misses", articleMetrics.getMisses());
        articleCache.put("total", articleMetrics.getTotal());
        articleCache.put("hitRatio", articleMetrics.getHitRatio());
        articleCache.put("hitRatioPercentage", articleMetrics.getHitRatioPercentage());

        metrics.put("articleCache", articleCache);

        return metrics;
    }

    @GetMapping("/refresh-article-cache")
    @ResponseBody
    public ResponseEntity<Map<String, String>> refreshArticleCache() {
        try {
            articleCacheService.refreshCache();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Article cache refresh triggered successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to refresh article cache: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}