package com.mvo.paymentprovider.notification.impl;

import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.Webhook;
import com.mvo.paymentprovider.mapper.CustomTransactionMapper;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {
    private final WebhookRepository webhookRepository;
    private final CustomTransactionMapper transactionMapper;
    private final WebClient webClient;
    private final int MAX_ATTEMPTS = 3;
    private final int DURATION_OF_PAUSES_BETWEEN_ATTEMPTS = 5;

    @Override
    public Mono<Transaction> sendNotification(Transaction transaction) {
        log.info("Sending notification for transaction: {}", transaction.getId());
        return createWebhookRecord(transaction)
                .doOnNext(webhook -> log.info("Created webhook for transaction id {} webhook id {}", transaction.getId(), webhook.getId()))
                .doOnError(error -> log.error("Error creating webhook for transaction id {}: {}", transaction.getId(), error.getMessage()))
                .flatMap(webhook -> webClient.post()
                        .uri(webhook.getNotificationUrl())
                        .bodyValue(transactionMapper.map(transaction))
                        .exchangeToMono(response -> handleWebhookResponse(response, webhook))
                        .thenReturn(transaction)
                        .retryWhen(retrySpec(webhook))
                        .onErrorResume(error -> handleFailedAttempt(webhook)
                                .then(Mono.error(error))
                        ));
    }

    private Mono<Webhook> createWebhookRecord(Transaction transaction) {
        Webhook webhook = Webhook.builder()
                .transactionId(transaction.getId())
                .notificationUrl(transaction.getNotificationUrl())
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return webhookRepository.save(webhook)
                .doOnError(error -> log.error("Error saving webhook for transaction id {}: {}", transaction.getId(), error.getMessage()));
    }

    private Mono<Void> handleFailedAttempt(Webhook webhook) {
        webhook.setStatus("FAILED");
        return saveWebhookWithAttemptUpdate(webhook)
                .doOnSuccess(savedWebhook -> log.info("Webhook marked as failed: {}", webhook.getId()))
                .doOnError(error -> log.error("Error saving failed webhook: {}", error.getMessage()))
                .then();
    }

    private Mono<Void> handleWebhookResponse(ClientResponse response, Webhook webhook) {
        log.info("Received response with status code: {}", response.statusCode());
        return updateAttemptsAndStatusWebhook(response, webhook).then();
    }

    private Mono<Void> updateAttemptsAndStatusWebhook(ClientResponse response, Webhook webhook) {
        if (response.statusCode().is2xxSuccessful()) {
            log.info("Processing successful attempt for webhook: {}", webhook.getId());
            processingSuccessfulAttempt(response, webhook);
        } else {
            log.warn("Processing failed attempt for webhook: {}", webhook.getId());
            processingFailedAttempt(response, webhook);
        }
        return saveWebhookWithAttemptUpdate(webhook);
    }

    private Mono<Void> saveWebhookWithAttemptUpdate(Webhook webhook) {
        webhook.setAttempts(webhook.getAttempts() + 1);
        webhook.setLastAttemptTime(LocalDateTime.now());
        webhook.setUpdatedAt(LocalDateTime.now());
        return webhookRepository.save(webhook)
                .doOnSuccess(savedWebhook -> log.info("Webhook updated: {}", savedWebhook.getId()))
                .doOnError(error -> log.error("Error saving webhook update: {}", error.getMessage()))
                .then();
    }

    private void processingSuccessfulAttempt(ClientResponse response, Webhook webhook) {
        webhook.setStatus("SUCCESS");
        webhook.setResponseStatus(response.statusCode().toString());
    }

    private void processingFailedAttempt(ClientResponse response, Webhook webhook) {
        webhook.setStatus("FAILED");
        webhook.setResponseStatus(response.statusCode().toString());
    }

    private Retry retrySpec(Webhook webhook) {
        return Retry.fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(DURATION_OF_PAUSES_BETWEEN_ATTEMPTS))
                .filter(this::filterResponseExceptions)
                .doBeforeRetry(retrySignal -> {
                    log.info("Preparing to retry for webhook: {}", webhook.getId());
                    saveWebhookWithAttemptUpdate(webhook)
                            .doOnError(e -> log.error("Error updating webhook before retry: {}", e.getMessage()))
                            .subscribe();
                });
    }

    private boolean filterResponseExceptions(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            boolean shouldRetry = !(ex.getStatusCode().is2xxSuccessful());
            if (shouldRetry) {
                log.info("Filtering response exception: {}", ex.getStatusCode());
            }
            return shouldRetry;
        }
        return true;
    }
}
