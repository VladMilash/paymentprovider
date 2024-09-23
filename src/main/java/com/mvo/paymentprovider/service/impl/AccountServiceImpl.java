package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.Account;
import com.mvo.paymentprovider.repository.AccountRepository;
import com.mvo.paymentprovider.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public Mono<Account> findByMerchantId(UUID merchantId) {
        return accountRepository.findByMerchantId(merchantId)
                .doOnSuccess(account -> log.info("Account with merchantId {} has been finding successfully", merchantId))
                .doOnError(error -> log.error("Failed to find account with merchantId {}", merchantId))
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Account> createAccount(Account account) {
        return accountRepository.save(account)
                .doOnSuccess(savedAccount -> log.info("account with id {} has been saved successfully", account.getId()))
                .doOnError(error -> log.error("Failed to saving account", error));
    }

    @Override
    public Mono<Account> findByMerchantIdAndCurrency(UUID merchantId, String currency) {
        return accountRepository.findByMerchantIdAndCurrency(merchantId, currency)
                .doOnSuccess(account -> log.info("Account with merchantId {} and currency {} has been found successfully", merchantId, currency))
                .doOnError(error -> log.error("Failed to find merchants account whit merchant id {}, and currency {} ", merchantId, currency))
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Account> findByCustomerIdAndCurrency(UUID customerId, String currency) {
        return accountRepository.findByCustomerIdAndCurrency(customerId, currency)
                .doOnSuccess(account -> log.info("Account with customerId {} and currency {} has been found successfully", customerId, currency))
                .doOnError(error -> log.error("Failed to find customers account whit customer id {}, and currency {} ", customerId, currency))
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Account> update(Account account) {
        return accountRepository.findById(account.getId())
                .map(account1 -> {
                    account1.setStatus(account.getStatus());
                    account1.setUpdatedAt(LocalDateTime.now());
                    account1.setOwnerType(account.getOwnerType());
                    account1.setCurrency(account.getCurrency());
                    account1.setBalance(account.getBalance());
                    account1.setMerchantId(account.getMerchantId());
                    account1.setCustomerId(account.getCustomerId());
                    return account1;
                })
                .flatMap(accountRepository::save)
                .doOnSuccess(event -> log.info("Account with id {} has been updated successfully", account.getId()))
                .doOnError(error -> log.error("Failed to update account with id {}", account.getId(), error));
    }
}
