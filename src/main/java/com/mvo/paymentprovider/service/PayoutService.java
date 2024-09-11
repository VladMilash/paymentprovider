package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PayoutService {
    Mono<Transaction> createPayout(String paymentMethod, BigDecimal amount, String currency,
                                   Long cardNumber, String language, String notificationUrl,
                                   String firstName, String lastName, String country, UUID merchantId);

    Mono<Transaction> updatePayoutStatus(UUID transactionId, TransactionStatus newStatus);

    Mono<Transaction> getPayoutDetails(UUID transactionId);

    Flux<Transaction> getPayoutsByCreatedAtBetween(LocalDate startDate, LocalDate endDate);
}
