package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import com.mvo.paymentprovider.service.TransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionServiceImpl implements TransactionService
{
    @Override
    public Mono<Transaction> createTransaction(String paymentMethod, BigDecimal amount, String currency, Long cardNumber, String expDate, String cvv, String language, String notificationUrl, String firstName, String lastName, String country, UUID merchantId) {
        return null;
    }

    @Override
    public Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus) {
        return null;
    }

    @Override
    public Flux<Transaction> getTransactionsByDay(LocalDate date) {
        return null;
    }

    @Override
    public Flux<Transaction> getTransactionsByPeriod(LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public Mono<Transaction> getTransactionDetails(UUID transactionId) {
        return null;
    }
}
