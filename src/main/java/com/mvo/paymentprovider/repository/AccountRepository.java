package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Account;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository extends R2dbcRepository<Account, UUID> {
    Mono<Account> findByMerchantIdAndCurrency(UUID merchantId, String currency);
    Mono<Account> findByCustomerIdAndCurrency(UUID merchantId, String currency);
}
