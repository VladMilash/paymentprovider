package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.Account;
import com.mvo.paymentprovider.repository.AccountRepository;
import com.mvo.paymentprovider.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public Mono<Account> createAccount(Account account) {
        return accountRepository.save(account)
                .doOnSuccess(savedAccount -> log.info("account with id {} has been saved successfully", account.getId()))
                .doOnError(error -> log.error("Failed to saving account", error));
    }

    @Override
    public Mono<Account> findByMerchantIdAndCurrency(UUID merchantId, String currency) {
        return accountRepository.findByMerchantIdAndCurrency(merchantId, currency)
                .doOnSuccess(account -> log.info("Account with merchantId {} and currency {} has been finding successfully", merchantId, currency))
                .doOnError(error -> log.error("Failed to find account with merchantId {} and currency {}", merchantId, currency));
    }

    @Override
    public Mono<Account> findByCustomerIdAndCurrency(UUID customerId, String currency) {
        return accountRepository.findByCustomerIdAndCurrency(customerId, currency)
                .doOnSuccess(account -> log.info("Account with customerId {} and currency {} has been finding successfully", customerId, currency))
                .doOnError(error -> log.error("Failed to find account with customerId {} and currency {}", customerId, currency));
    }
}
