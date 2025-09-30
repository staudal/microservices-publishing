package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    private static final Logger log = LoggerFactory.getLogger(PublisherService.class);

    private final DraftService draftService;
    private final RabbitTemplate rabbitTemplate;
    private static final String QUEUE_NAME = "article-queue";

    public PublisherService(DraftService draftService, RabbitTemplate rabbitTemplate) {
        this.draftService = draftService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDraft(Long draftId, String continent) {
        log.info("Publishing draft to queue: draftId={}, continent={}", draftId, continent);

        Draft draft = draftService.readById(draftId);
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