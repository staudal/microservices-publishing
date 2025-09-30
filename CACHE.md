# Cache Implementation

## Overview
Two Redis-based cache layers improve availability and response times for geographically distributed users.

## 1. Article Cache (GLOBAL articles)

**Strategy**: Offline refresh (scheduled preloading)

- **Location**: Between ArticleService and GLOBAL PostgreSQL database
- **Refresh**: Hourly via scheduled task (`ArticleCacheService:32`)
- **TTL**: 14 days
- **Scope**: Only GLOBAL continent articles (other continents bypass cache)
- **Manual trigger**: `GET /metrics/refresh-article-cache`

## 2. Comment Cache

**Strategy**: Cache-aside (cache-miss approach with LRU eviction)

- **Location**: Between CommentService and Comment PostgreSQL database
- **Capacity**: 30 most recently accessed articles
- **Eviction**: LRU via `LinkedHashMap` (in-memory) + Redis (data storage)
- **TTL**: 1 day
- **Automatic**: Populates on first access, evicts least recently used

## Metrics & Monitoring

**Dashboard**: `http://localhost:8082/metrics/dashboard`
- Auto-refreshes every 5 seconds
- Shows hit ratio, hits, misses for both caches
- Metrics shared across all 3 article-service instances via Redis

**API**: `http://localhost:8082/metrics/cache` (JSON)

## Architecture Notes

- Redis runs with LRU eviction (`allkeys-lru`) and 256MB max memory
- Metrics stored in Redis, shared across load-balanced instances
- Article cache requires manual/scheduled refresh to populate
- Comment cache auto-populates on demand

## Testing

```bash
# Manually refresh article cache
curl http://localhost:8082/metrics/refresh-article-cache

# Fetch GLOBAL article (uses cache after refresh)
curl "http://localhost:8080/articles/1?continent=GLOBAL"

# Fetch comments (auto-populates cache)
curl http://localhost:8082/comments/article/1
```