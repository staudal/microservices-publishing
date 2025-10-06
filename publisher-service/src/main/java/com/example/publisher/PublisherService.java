package com.example.publisher;

import com.example.shared.model.ArticleQueueMessage;
import com.example.shared.model.Draft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PublisherService {

    private static final Logger log = LoggerFactory.getLogger(PublisherService.class);

    @Autowired
    private RestTemplate restTemplate;

    private final RabbitTemplate rabbitTemplate;
    private static final String QUEUE_NAME = "article-queue";
    private static final String DRAFT_SERVICE_URL = "http://draft-service:8080/drafts/";

    public PublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDraft(Long draftId, String continent) {
        log.info("Publishing draft to queue: draftId={}, continent={}", draftId, continent);

        // Call draft-service via HTTP to get the draft
        Draft draft = restTemplate.getForObject(DRAFT_SERVICE_URL + draftId, Draft.class);

        if (draft == null) {
            log.error("Draft not found: draftId={}", draftId);
            throw new IllegalArgumentException("Draft not found with id: " + draftId);
        }

        ArticleQueueMessage message = new ArticleQueueMessage(
                draft.getId(),
                draft.getTitle(),
                continent
        );

        rabbitTemplate.convertAndSend(QUEUE_NAME, message);
        log.info("Draft published successfully to queue: draftId={}, title={}, continent={}",
                draftId, draft.getTitle(), continent);
    }
}
