package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository extends R2dbcRepository<Account, UUID> {
    @Query("SELECT * FROM account WHERE merchant_id = :merchantId AND currency = :currency")
    Mono<Account> findByMerchantIdAndCurrency(UUID merchantId, String currency);

    @Query("SELECT * FROM account WHERE customer_id = :customerId AND currency = :currency FOR UPDATE")
    Mono<Account> findByCustomerIdAndCurrency(UUID merchantId, String currency);

    @Query("SELECT * FROM account WHERE merchant_id = :merchantId")
    Mono<Account> findByMerchantId(UUID merchantId);
}
