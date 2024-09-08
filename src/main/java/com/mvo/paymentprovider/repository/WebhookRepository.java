package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Webhook;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WebhookRepository extends R2dbcRepository<Webhook, UUID> {
    Mono<Webhook> findByTransactionId(UUID transactionId);

}
