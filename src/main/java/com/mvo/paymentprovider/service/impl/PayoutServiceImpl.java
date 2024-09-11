package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import com.mvo.paymentprovider.service.PayoutService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PayoutServiceImpl implements PayoutService {
    @Override
    public Mono<Transaction> createPayout(String paymentMethod, BigDecimal amount, String currency, Long cardNumber, String language, String notificationUrl, String firstName, String lastName, String country, UUID merchantId) {
        return null;
    }

    @Override
    public Mono<Transaction> updatePayoutStatus(UUID transactionId, TransactionStatus newStatus) {
        return null;
    }

    @Override
    public Mono<Transaction> getPayoutDetails(UUID transactionId) {
        return null;
    }

    @Override
    public Flux<Transaction> getPayoutsByPeriod(LocalDate startDate, LocalDate endDate) {
        return null;
    }
}
