package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.dto.CardDTO;
import com.mvo.paymentprovider.dto.CustomerDTO;
import com.mvo.paymentprovider.dto.MerchantDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.TransactionRepository;
import com.mvo.paymentprovider.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutServiceImpl implements PayoutService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final MerchantService merchantService;
    private final CustomerService customerService;
    private final WebhookService webhookService;

    @Override
    @Transactional
    public Mono<Transaction> createPayout(TransactionDTO transactionDTO, CardDTO cardDTO,
                                          CustomerDTO customerDTO, MerchantDTO merchantDTO) {
        log.info("Creating payout: paymentMethod {}, amount {}, currency {}, merchantId {}",
                transactionDTO.getPaymentMethod(), transactionDTO.getAmount(), transactionDTO.getCurrency(), merchantDTO.getId());

        return merchantService.findById(merchantDTO.getId())
                .switchIfEmpty(Mono.error(new RuntimeException("Merchant not found")))
                .flatMap(merchant -> {
                    log.info("Merchant with id {} found and is active", merchantDTO.getId());

                    return accountService.findByMerchantIdAndCurrency(merchantDTO.getId(), transactionDTO.getCurrency())
                            .switchIfEmpty(Mono.error(new RuntimeException("Merchant account not found")))
                            .flatMap(merchantAccount -> {
                                log.info("Merchant account for merchantId {} and currency {} found",
                                        merchantDTO.getId(), transactionDTO.getCurrency());

                                return customerService.findByFirstnameAndLastnameAndCountry(customerDTO.getFirstname(),
                                                customerDTO.getLastname(), customerDTO.getCountry())
                                        .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")))
                                        .flatMap(customer -> {
                                            log.info("Customer with name {} {} found", customerDTO.getFirstname(),
                                                    customerDTO.getLastname());

                                            return accountService.findByCustomerIdAndCurrency(customer.getId(),
                                                            transactionDTO.getCurrency())
                                                    .switchIfEmpty(Mono.error(new RuntimeException("Customer account not found")))
                                                    .flatMap(customerAccount -> {
                                                        log.info("Customer account for customerId {} and currency {} found",
                                                                customer.getId(), transactionDTO.getCurrency());

                                                        return processPayout(customerAccount, merchantAccount, transactionDTO)
                                                                .flatMap(transaction -> {
                                                                    log.info("Payout transaction for amount {} created",
                                                                            transactionDTO.getAmount());

                                                                    return transactionRepository.save(transaction)
                                                                            .flatMap(savedTransaction -> {
                                                                                log.info("Payout transaction with id {} successfully saved",
                                                                                        savedTransaction.getId());
                                                                                webhookService.sendNotification(savedTransaction).subscribe();
                                                                                return Mono.just(savedTransaction);
                                                                            });
                                                                });
                                                    });
                                        });
                            });
                })
                .doOnError(error -> log.error("Failed to create payout: {}", error.getMessage(), error));
    }

    @Override
    @Transactional
    public Mono<Transaction> updatePayoutStatus(UUID transactionId, TransactionStatus newStatus) {
        return transactionRepository.findById(transactionId)
                .map(transaction -> {
                    transaction.setTransactionStatus(newStatus);
                    return transaction;
                })
                .flatMap(transactionRepository::save)
                .doOnSuccess(transaction -> log.info("Payout status with id {} has been updated successfully, new status {}",
                        transactionId, newStatus))
                .doOnError(error -> log.error("Failed to update payout status transaction with id {}",
                        transactionId, error));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Transaction> getPayoutDetails(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .doOnSuccess(transaction -> log.info("Payout with id {} has been found successfully", transactionId))
                .doOnError(error -> log.error("Failed to find Payout with id {}", transactionId, error));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Transaction> getPayoutsByCreatedAtBetween(LocalDate startDate, LocalDate endDate) {
        OperationType operationType = OperationType.PAYOUT;
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return transactionRepository.getTransactionsByCreatedAtBetweenAndOperationType(start, end, operationType)
                .doOnNext(transaction -> log.info("Payouts by period {} {} have been found successfully",
                        startDate, endDate))
                .doOnError(error -> log.error("Failed to find Payouts by period {} {}",
                        startDate, endDate, error));
    }

    private Mono<Transaction> processPayout(Account customerAccount, Account merchantAccount, TransactionDTO transactionDTO) {
        if (merchantAccount.getBalance().compareTo(transactionDTO.getAmount()) < 0) {
            return Mono.error(new RuntimeException("There are not enough funds in the merchant's account for payment"));
        }

        merchantAccount.setBalance(merchantAccount.getBalance().subtract(transactionDTO.getAmount()));
        return accountService.update(merchantAccount)
                .flatMap(updatedMerchantAccount -> {
                    customerAccount.setBalance(customerAccount.getBalance().add(transactionDTO.getAmount()));
                    return accountService.update(customerAccount)
                            .flatMap(updatedCustomerAccount -> {
                                Transaction transaction = createTransactionRecord(updatedCustomerAccount,
                                        updatedMerchantAccount, transactionDTO);
                                return transactionRepository.save(transaction);
                            });
                });
    }

    private Transaction createTransactionRecord(Account customerAccount, Account merchantAccount, TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setCustomerAccountId(customerAccount.getId());
        transaction.setMerchantAccountId(merchantAccount.getId());
        transaction.setOperationType(OperationType.PAYOUT);
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setCurrency(customerAccount.getCurrency());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.IN_PROGRESS);
        transaction.setLanguage(transactionDTO.getLanguage());
        transaction.setMessage("Payout in progress");
        transaction.setNotificationUrl(transactionDTO.getNotificationUrl());
        transaction.setPaymentMethod(transactionDTO.getPaymentMethod());
        return transaction;
    }
}