package com.mvo.paymentprovider.notification.impl;

import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.Webhook;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {
    private final WebhookRepository webhookRepository;
    private final TransactionMapper transactionMapper;
    private final WebClient webClient;

    @Override
    public Mono<Transaction> sendNotification(Transaction transaction) {
        return webhookRepository.findByTransactionId(transaction.getId())
                .flatMap(webhook -> webClient.post()
                        .uri(webhook.getNotificationUrl())
                        .bodyValue(transactionMapper.map(transaction))
                        .exchangeToMono(response -> handleWebhookResponse(response, webhook))
                        .thenReturn(transaction)
                        .doOnError(error -> handleFailedAttempt(webhook)));
    }

    private void handleFailedAttempt(Webhook webhook) {
        webhook.setStatus("FAILED");
        webhook.setAttempts(webhook.getAttempts() + 1);
        webhook.setLastAttemptTime(LocalDateTime.now());
        webhookRepository.save(webhook);
    }

    private Mono<Void> handleWebhookResponse(ClientResponse response, Webhook webhook) {
        if (response.statusCode().is2xxSuccessful()) {
            webhook.setStatus("SUCCESS");
            webhook.setResponseStatus("200 OK");
        } else {
            webhook.setStatus("FAILED");
            webhook.setResponseStatus(response.statusCode().toString());
        }
        webhook.setAttempts(webhook.getAttempts() + 1);
        webhook.setLastAttemptTime(LocalDateTime.now());
        return webhookRepository.save(webhook).then();
    }
}
