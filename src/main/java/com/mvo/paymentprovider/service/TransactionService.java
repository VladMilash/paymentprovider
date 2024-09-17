package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.dto.CardDTO;
import com.mvo.paymentprovider.dto.CustomerDTO;
import com.mvo.paymentprovider.dto.MerchantDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionService {
    Mono<Transaction> createTransaction(TransactionDTO transactionDTO, CardDTO cardDTO,
                                        CustomerDTO customerDTO, MerchantDTO merchantDTO);

    Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus);

    public Flux<Transaction> getTransactionsByCreatedAtBetween(LocalDate startDate, LocalDate endDate);

    Mono<Transaction> getTransactionDetails(UUID transactionId);


}
