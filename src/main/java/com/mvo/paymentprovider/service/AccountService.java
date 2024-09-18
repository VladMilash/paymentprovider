package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.entity.Account;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountService {
    Mono<Account> createAccount(Account account);

    Mono<Account> findByMerchantIdAndCurrency(UUID merchantId, String currency);

    Mono<Account> findByCustomerIdAndCurrency(UUID customerId, String currency);

    Mono<Account> update(Account account);

    Mono<Account> findByMerchantId(UUID merchantId);

}
