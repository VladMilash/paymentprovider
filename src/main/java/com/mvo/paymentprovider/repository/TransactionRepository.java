package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Transaction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TransactionRepository extends R2dbcRepository<Transaction, Long> {
}
