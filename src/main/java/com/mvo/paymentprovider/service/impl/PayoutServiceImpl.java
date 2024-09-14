package com.mvo.paymentprovider.service.impl;

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

import java.math.BigDecimal;
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
    public Mono<Transaction> createPayout(String paymentMethod, BigDecimal amount, String currency,
                                          Long cardNumber, String language, String notificationUrl,
                                          String firstName, String lastName, String country, UUID merchantId) {
        log.info("Creating payout: paymentMethod {}, amount {}, currency {}, merchantId {}", paymentMethod, amount, currency, merchantId);

        return merchantService.findById(merchantId)
                .switchIfEmpty(Mono.error(new RuntimeException("Merchant not found")))
                .flatMap(merchant -> {
                    if (!merchant.getStatus().equals(Status.ACTIVE)) {
                        log.error("Merchant with id {} is not active", merchantId);
                        return Mono.error(new RuntimeException("Merchant is not active"));
                    }
                    log.info("Merchant with id {} found and is active", merchantId);

                    return accountService.findByMerchantIdAndCurrency(merchantId, currency)
                            .switchIfEmpty(Mono.error(new RuntimeException("Merchant account not found")))
                            .flatMap(merchantAccount -> {
                                log.info("Merchant account for merchantId {} and currency {} found", merchantId, currency);

                                return customerService.findByFirstnameAndLastnameAndCountry(firstName, lastName, country)
                                        .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")))
                                        .flatMap(customer -> {
                                            log.info("Customer with name {} {} found", firstName, lastName);

                                            return accountService.findByCustomerIdAndCurrency(customer.getId(), currency)
                                                    .switchIfEmpty(Mono.error(new RuntimeException("Customer account not found")))
                                                    .flatMap(customerAccount -> {
                                                        log.info("Customer account for customerId {} and currency {} found", customer.getId(), currency);

                                                        return processPayout(customerAccount, merchantAccount, amount, paymentMethod, language, notificationUrl)
                                                                .flatMap(transaction -> {
                                                                    log.info("Payout transaction for amount {} created", amount);

                                                                    return transactionRepository.save(transaction)
                                                                            .flatMap(savedTransaction -> {
                                                                                log.info("Payout transaction with id {} successfully saved", savedTransaction.getId());
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
                .doOnSuccess(transaction -> log.info("Payout status with id {} has been updated successfully< new status {}", transactionId, newStatus))
                .doOnError(error -> log.error("Failed to update payout status transaction with id {}", transactionId, error));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Transaction> getPayoutDetails(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .doOnSuccess(transaction -> log.info("Payout with id {} has been find successfully", transactionId))
                .doOnError(error -> log.error("Failed to find Payout with id {}", transactionId, error));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Transaction> getPayoutsByCreatedAtBetween(LocalDate startDate, LocalDate endDate) {
        OperationType operationType = OperationType.PAYOUT;
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return transactionRepository.getTransactionsByCreatedAtBetweenAndOperationType(start, end, operationType)
                .doOnNext(transaction -> log.info("Payouts by period {} {} has been find successfully", startDate, endDate))
                .doOnError(error -> log.error("Failed to find Payouts by period {} {}", startDate, endDate, error));
    }

    private Mono<Transaction> processPayout(Account merchantAccount, Account customerAccount, BigDecimal amount,
                                            String paymentMethod, String language, String notificationUrl) {
        if (merchantAccount.getBalance().compareTo(amount) < 0) {
            return Mono.error(new RuntimeException("There are not enough funds in the merchant's account for payment"));
        }

        merchantAccount.setBalance(merchantAccount.getBalance().subtract(amount));
        return accountService.update(merchantAccount)
                .flatMap(updatedMerchantAccount -> {
                    customerAccount.setBalance(customerAccount.getBalance().add(amount));
                    return accountService.update(customerAccount)
                            .flatMap(updatedCustomerAccount -> {
                                Transaction transaction = createTransactionRecord(updatedMerchantAccount, updatedCustomerAccount, amount,
                                        language, notificationUrl, paymentMethod);
                                return transactionRepository.save(transaction);
                            });
                });
    }


    private Transaction createTransactionRecord(Account customerAccount, Account merchantAccount, BigDecimal amount,
                                                String language, String notificationUrl, String paymentMethod) {
        Transaction transaction = new Transaction();
        transaction.setCustomerAccountId(customerAccount.getId());
        transaction.setMerchantAccountId(merchantAccount.getId());
        transaction.setOperationType(OperationType.PAYOUT);
        transaction.setAmount(amount);
        transaction.setCurrency(customerAccount.getCurrency());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.IN_PROGRESS);
        transaction.setLanguage(language);
        transaction.setMessage("Payout in progress");
        transaction.setNotificationUrl(notificationUrl);
        transaction.setPaymentMethod(paymentMethod);
        return transaction;
    }
}
