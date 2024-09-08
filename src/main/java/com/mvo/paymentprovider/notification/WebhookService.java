package com.mvo.paymentprovider.notification;

import com.mvo.paymentprovider.entity.Transaction;
import reactor.core.publisher.Mono;

public interface WebhookService {
    Mono<Transaction> sendNotification(Transaction transaction);
}
