package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.dto.RequestDTO;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.exception.ApiException;
import com.mvo.paymentprovider.exception.NotFoundEntityException;
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
import java.time.LocalTime;
import java.time.ZoneId;
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
    public Mono<Transaction> createPayout(RequestDTO requestDTO) {
        log.info("Creating payout: paymentMethod {}, amount {}, currency {}, merchantId {}",
                requestDTO.getPaymentMethod(), requestDTO.getAmount(), requestDTO.getCurrency(), requestDTO.getMerchantId());

        return merchantService.findById(requestDTO.getMerchantId())
                .switchIfEmpty(Mono.error(new NotFoundEntityException("Merchant is not found", "NOT_FOUND_MERCHANT")))
                .flatMap(merchant -> {
                    log.info("Merchant with id {} found and is active", requestDTO.getMerchantId());

                    return accountService.findByMerchantIdAndCurrency(requestDTO.getMerchantId(), requestDTO.getCurrency())
                            .switchIfEmpty(Mono.error(new NotFoundEntityException("Merchant account is not found", "NOT_FOUND_MERCHANT_ACCOUNT")))
                            .flatMap(merchantAccount -> {
                                log.info("Merchant account for merchantId {} and currency {} found",
                                        requestDTO.getMerchantId(), requestDTO.getCurrency());

                                return customerService.findByFirstnameAndLastnameAndCountry(requestDTO.getFirstName(),
                                                requestDTO.getLastName(), requestDTO.getCountry())
                                        .switchIfEmpty(Mono.error(new NotFoundEntityException("Customer is not found", "NOT_FOUND_CUSTOMER")))
                                        .flatMap(customer -> {
                                            log.info("Customer with name {} {} found", requestDTO.getFirstName(),
                                                    requestDTO.getLastName());

                                            return accountService.findByCustomerIdAndCurrency(customer.getId(),
                                                            requestDTO.getCurrency())
                                                    .switchIfEmpty(Mono.error(new NotFoundEntityException("Customer account is not found", "NOT_FOUND_CUSTOMER_ACCOUNT")))
                                                    .flatMap(customerAccount -> {
                                                        log.info("Customer account for customerId {} and currency {} found",
                                                                customer.getId(), requestDTO.getCurrency());

                                                        return processPayout(customerAccount, merchantAccount, requestDTO)
                                                                .flatMap(transaction -> {
                                                                    log.info("Payout transaction for amount {} created",
                                                                            requestDTO.getAmount());

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
    public Flux<Transaction> getPayoutsByCreatedAtBetween(LocalDate startDate, LocalDate endDate, UUID merchantID) {

        OperationType operationType = OperationType.PAYOUT;
        ZoneId zoneId = ZoneId.of("UTC");
        LocalDateTime start = startDate.atStartOfDay(zoneId).toLocalDateTime();
        LocalDateTime end = endDate.atTime(LocalTime.MAX).atZone(zoneId).toLocalDateTime();

        return accountService.findByMerchantId(merchantID)
                .switchIfEmpty(Mono.error(new NotFoundEntityException("Merchant account is not found", "NOT_FOUND_MERCHANT_ACCOUNT")))
                .flatMapMany(account -> transactionRepository.getTransactionsByCreatedAtBetweenAndOperationTypeAndMerchantAccountId(start, end,
                                operationType, account.getId())
                        .doOnNext(transaction -> log.info("Payouts by period {} {} have been found successfully",
                                startDate, endDate))
                        .doOnError(error -> log.error("Failed to find Payouts by period {} {}",
                                startDate, endDate, error))
                );
    }

    private Mono<Transaction> processPayout(Account customerAccount, Account merchantAccount, RequestDTO requestDTO) {
        if (merchantAccount.getBalance().compareTo(requestDTO.getAmount()) < 0) {
            return Mono.error(new ApiException("There are not enough funds in the merchant's account for payment", "FROM_MERCHANT_PAYMENTS_ERROR"));
        }

        merchantAccount.setBalance(merchantAccount.getBalance().subtract(requestDTO.getAmount()));
        return accountService.update(merchantAccount)
                .flatMap(updatedMerchantAccount -> {
                    customerAccount.setBalance(customerAccount.getBalance().add(requestDTO.getAmount()));
                    return accountService.update(customerAccount)
                            .flatMap(updatedCustomerAccount -> {
                                Transaction transaction = createTransactionRecord(updatedCustomerAccount,
                                        updatedMerchantAccount, requestDTO);
                                return Mono.just(transaction);
                            });
                });
    }

    private Transaction createTransactionRecord(Account customerAccount, Account merchantAccount, RequestDTO requestDTO) {
        Transaction transaction = new Transaction();
        transaction.setCustomerAccountId(customerAccount.getId());
        transaction.setMerchantAccountId(merchantAccount.getId());
        transaction.setOperationType(OperationType.PAYOUT);
        transaction.setAmount(requestDTO.getAmount());
        transaction.setCurrency(customerAccount.getCurrency());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.IN_PROGRESS);
        transaction.setLanguage(requestDTO.getLanguage());
        transaction.setMessage("Payout in progress");
        transaction.setNotificationUrl(requestDTO.getNotificationUrl());
        transaction.setPaymentMethod(requestDTO.getPaymentMethod());
        return transaction;
    }
}