package com.example.comment;

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

    @GetMapping("/cache")
    @ResponseBody
    public Map<String, Object> getCacheMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        CacheMetricsService.CacheMetrics commentMetrics = cacheMetricsService.getCommentCacheMetrics();

        Map<String, Object> commentCache = new HashMap<>();
        commentCache.put("hits", commentMetrics.getHits());
        commentCache.put("misses", commentMetrics.getMisses());
        commentCache.put("total", commentMetrics.getTotal());
        commentCache.put("hitRatio", commentMetrics.getHitRatio());
        commentCache.put("hitRatioPercentage", commentMetrics.getHitRatioPercentage());

        metrics.put("commentCache", commentCache);

        return metrics;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        CacheMetricsService.CacheMetrics commentMetrics = cacheMetricsService.getCommentCacheMetrics();
        CacheMetricsService.CacheMetrics articleMetrics = cacheMetricsService.getArticleCacheMetrics();

        model.addAttribute("commentMetrics", commentMetrics);
        model.addAttribute("articleMetrics", articleMetrics);

        return "cache-dashboard";
    }
}
