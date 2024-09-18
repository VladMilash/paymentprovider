package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.dto.*;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PayoutService {
    Mono<Transaction> createPayout(RequestDTO requestDTO);

    Mono<Transaction> updatePayoutStatus(UUID transactionId, TransactionStatus newStatus);

    Mono<Transaction> getPayoutDetails(UUID transactionId);

    Flux<Transaction> getPayoutsByCreatedAtBetween(LocalDate startDate, LocalDate endDate);
}
