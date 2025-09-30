package com.example.demo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    private final DraftService draftService;
    private final RabbitTemplate rabbitTemplate;
    private static final String QUEUE_NAME = "article-queue";

    public PublisherService(DraftService draftService, RabbitTemplate rabbitTemplate) {
        this.draftService = draftService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDraft(Long draftId, String continent) {
        Draft draft = draftService.readById(draftId);
        if (draft == null) {
            throw new IllegalArgumentException("Draft not found with id: " + draftId);
        }

        ArticleQueueMessage message = new ArticleQueueMessage(
                draft.getId(),
                draft.getTitle(),
                continent
        );

        rabbitTemplate.convertAndSend(QUEUE_NAME, message);
    }
}