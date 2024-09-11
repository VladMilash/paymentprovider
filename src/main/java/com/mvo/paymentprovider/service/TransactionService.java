package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionService {
    Mono<Transaction> createTransaction(String paymentMethod, BigDecimal amount, String currency,
                                        Long cardNumber, String expDate, String cvv,
                                        String language, String notificationUrl, String firstName,
                                        String lastName, String country, UUID merchantId);

    Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus);

    public Flux<Transaction> getTransactionsByCreatedAtBetween(LocalDate startDate, LocalDate endDate, OperationType operationType);

    Mono<Transaction> getTransactionDetails(UUID transactionId);


}
