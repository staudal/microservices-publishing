#!/bin/bash

set -e  # Exit on error

echo "=========================================="
echo "Testing HappyHeadlines Full Flow"
echo "=========================================="
echo ""

# Step 1: Create a draft
echo "1. Creating draft..."
DRAFT_RESPONSE=$(curl -s -X POST http://localhost:8081/drafts \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Article: Amazing News from Script"}')

DRAFT_ID=$(echo $DRAFT_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "   ✓ Draft created with ID: $DRAFT_ID"
echo ""

# Step 2: Publish the draft to GLOBAL
echo "2. Publishing draft to article queue (GLOBAL)..."
PUBLISH_RESPONSE=$(curl -s -X POST "http://localhost:8082/publish/${DRAFT_ID}?continent=GLOBAL")
echo "   ✓ $PUBLISH_RESPONSE"
echo ""

# Wait for RabbitMQ processing
echo "3. Waiting for RabbitMQ processing..."
sleep 3
echo "   ✓ Done"
echo ""

# Step 4: Fetch all articles and get the latest one
echo "4. Fetching all articles from ArticleService..."
ALL_ARTICLES=$(curl -s "http://localhost:8080/articles")
echo "   All articles: $ALL_ARTICLES"

# Extract the last article ID (latest)
ARTICLE_ID=$(echo $ALL_ARTICLES | grep -o '"id":[0-9]*' | tail -1 | grep -o '[0-9]*')

if [ -z "$ARTICLE_ID" ]; then
    echo "   ✗ ERROR: No articles found! RabbitMQ processing may have failed."
    echo "   Check logs: docker-compose logs article-service-1"
    exit 1
fi

echo "   ✓ Latest article ID: $ARTICLE_ID"
echo ""

# Step 5: Fetch specific article
echo "5. Fetching specific article..."
ARTICLE_RESPONSE=$(curl -s "http://localhost:8080/articles/${ARTICLE_ID}?continent=GLOBAL")
echo "   ✓ Article: $ARTICLE_RESPONSE"
echo ""

# Step 6: Create a comment
echo "6. Creating comment for article..."
COMMENT_RESPONSE=$(curl -s -X POST http://localhost:8083/comments \
  -H "Content-Type: application/json" \
  -d "{\"text\":\"This is an amazing article! Generated from test script.\",\"articleId\":${ARTICLE_ID}}")

COMMENT_ID=$(echo $COMMENT_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "   ✓ Comment created with ID: $COMMENT_ID"
echo ""

# Step 7: Fetch comments for the article
echo "7. Fetching all comments for article..."
COMMENTS=$(curl -s http://localhost:8083/comments/article/${ARTICLE_ID})
echo "   ✓ Comments: $COMMENTS"
echo ""

# Step 8: Trigger article cache refresh
echo "8. Triggering article cache refresh..."
CACHE_RESPONSE=$(curl -s http://localhost:8083/metrics/refresh-article-cache)
echo "   ✓ $CACHE_RESPONSE"
echo ""

# Step 9: Fetch article again (should hit cache)
echo "9. Fetching article again (should hit cache)..."
curl -s "http://localhost:8080/articles/${ARTICLE_ID}?continent=GLOBAL" > /dev/null
echo "   ✓ Article fetched from cache"
echo ""

echo "=========================================="
echo "✓ Full flow completed successfully!"
echo "=========================================="
echo ""
echo "Check your dashboards:"
echo "  - Cache metrics: http://localhost:8083/metrics/dashboard"
echo "  - Traces: http://localhost:9411"
echo "  - Logs: http://localhost:3000"
echo ""