package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.entity.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionService {
    Flux<Transaction> getTransactionsByDay(Long merchantId, LocalDateTime date);

    Flux<Transaction> getTransactionsByPeriod(Long merchantId, LocalDateTime startDate, LocalDateTime finishDate);

    Mono<Transaction> saveTransaction(Transaction transaction);

    Mono<Transaction> updateTransactionStatus(Transaction transaction);

    Mono<Transaction> getTransactionById(Long transactionId);

    Mono<Transaction> savePayout(Transaction transaction);

    Mono<Transaction> getPayoutById(Long transactionId);

    Flux<Transaction> getPayoutsByDay(Long merchantId, LocalDateTime date);

}
