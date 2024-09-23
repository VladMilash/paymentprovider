package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.dto.RequestDTO;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.exception.NotFoundEntityException;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.*;
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
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CardService cardService;
    private final MerchantService merchantService;
    private final CustomerService customerService;
    private final WebhookService webhookService;

    @Override
    @Transactional
    public Mono<Transaction> createTransaction(RequestDTO requestDTO) {
        log.info("Creating transaction: paymentMethod {}, amount {}, currency {}, merchantId {}",
                requestDTO.getPaymentMethod(), requestDTO.getAmount(), requestDTO.getCurrency(), requestDTO.getMerchantId());

        return merchantService.findById(requestDTO.getMerchantId())
                .switchIfEmpty(Mono.error(new NotFoundEntityException("Merchant is not found", "NOT_FOUND_MERCHANT")))
                .flatMap(merchant -> accountService.findByMerchantIdAndCurrency(requestDTO.getMerchantId(), requestDTO.getCurrency())
                        .switchIfEmpty(Mono.error(new NotFoundEntityException("Merchant account is not found", "NOT_FOUND_MERCHANT_ACCOUNT")))
                        .flatMap(merchantAccount -> {
                            log.info("Merchant account for merchantId {} and currency {} found",
                                    requestDTO.getMerchantId(), requestDTO.getCurrency());

                            return customerService.findByFirstnameAndLastnameAndCountry(requestDTO.getFirstName(),
                                            requestDTO.getLastName(), requestDTO.getCountry())
                                    .switchIfEmpty(Mono.defer(() -> {
                                        log.info("Customer not found, creating new customer: firstName {}, lastName {}, country {}",
                                                requestDTO.getFirstName(), requestDTO.getLastName(), requestDTO.getCountry());
                                        return customerService.createCustomer(createCustomer(requestDTO));
                                    }))
                                    .flatMap(customer -> {
                                        log.info("Customer with name {} {} found/created",
                                                requestDTO.getFirstName(), requestDTO.getLastName());

                                        return accountService.findByCustomerIdAndCurrency(customer.getId(), requestDTO.getCurrency())
                                                .switchIfEmpty(Mono.defer(() -> {
                                                    log.info("Customer account not found, creating new account for customerId {} and currency {}",
                                                            customer.getId(), requestDTO.getCurrency());
                                                    return accountService.createAccount(createAccount(customer, requestDTO.getCurrency()));
                                                }))
                                                .flatMap(customerAccount -> {
                                                    log.info("Customer account found/created for customerId {} and currency {}",
                                                            customer.getId(), requestDTO.getCurrency());

                                                    return cardService.findByCardNumber(requestDTO.getCardNumber())
                                                            .switchIfEmpty(Mono.defer(() -> {
                                                                log.info("Card not found, creating new card for cardNumber {}", requestDTO.getCardNumber());
                                                                return cardService.createCard(createCard(customerAccount, requestDTO));
                                                            }))
                                                            .flatMap(card -> {
                                                                log.info("Card with cardNumber {} found/created", requestDTO.getCardNumber());

                                                                return topUpMerchantAccount(customerAccount, merchantAccount, requestDTO)
                                                                        .flatMap(transaction -> {
                                                                            log.info("Transaction for amount {} and paymentMethod {} is in progress",
                                                                                    requestDTO.getAmount(), requestDTO.getPaymentMethod());

                                                                            return transactionRepository.save(transaction)
                                                                                    .flatMap(savedTransaction -> {
                                                                                        log.info("Transaction with id {} successfully saved", savedTransaction.getId());
                                                                                        webhookService.sendNotification(transaction).subscribe();
                                                                                        return Mono.just(savedTransaction);
                                                                                    });
                                                                        });
                                                            });
                                                });
                                    });
                        }))
                .doOnError(error -> log.error("Failed to create transaction: {}", error.getMessage(), error));
    }

    @Override
    @Transactional
    public Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus) {
        return transactionRepository.findById(transactionId)
                .map(transaction -> {
                    transaction.setTransactionStatus(newStatus);
                    return transaction;
                })
                .flatMap(transactionRepository::save)
                .doOnSuccess(transaction -> log.info("Transaction status with id {} has been updated successfully, new status {}",
                        transactionId, newStatus))
                .doOnError(error -> log.error("Failed to update transaction status for transaction with id {}", transactionId, error));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Transaction> getTransactionsByCreatedAtBetween(LocalDate startDate, LocalDate endDate, UUID merchantID) {

        OperationType operationType = OperationType.TOP_UP;
        ZoneId zoneId = ZoneId.of("UTC");
        LocalDateTime start = startDate.atStartOfDay(zoneId).toLocalDateTime();
        LocalDateTime end = endDate.atTime(LocalTime.MAX).atZone(zoneId).toLocalDateTime();

        log.info("Retrieving transactions for Merchant ID: {}", merchantID);
        log.info("Searching transactions between {} and {}", start, end);

        return accountService.findByMerchantId(merchantID)
                .doOnSubscribe(subscription -> log.info("Attempting to find account for Merchant ID: {}", merchantID))
                .switchIfEmpty(Mono.error(new NotFoundEntityException("Merchant account is not found", "NOT_FOUND_MERCHANT_ACCOUNT")))
                .flatMapMany(account -> {
                    log.info("Found account: {}", account);
                    return transactionRepository.getTransactionsByCreatedAtBetweenAndOperationTypeAndMerchantAccountId(start, end,
                                    operationType, account.getId())
                            .doOnNext(transaction -> log.info("Transaction found: {}", transaction))
                            .doOnComplete(() -> log.info("Completed fetching transactions for Merchant ID: {}", merchantID))
                            .doOnError(error -> log.error("Error fetching transactions for Merchant ID: {}. Error: {}", merchantID, error));
                })
                .doOnError(error -> log.error("Failed to find transactions for Merchant ID: {}. Error: {}", merchantID, error));
    }



    @Override
    @Transactional(readOnly = true)
    public Mono<Transaction> getTransactionDetails(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .doOnSuccess(transaction -> log.info("Transaction with id {} has been found successfully", transactionId))
                .doOnError(error -> log.error("Failed to find transaction with id {}", transactionId, error));
    }

    private Mono<Transaction> topUpMerchantAccount(Account customerAccount, Account merchantAccount, RequestDTO requestDTO) {
        customerAccount.setBalance(customerAccount.getBalance().subtract(requestDTO.getAmount()));
        return accountService.update(customerAccount)
                .flatMap(updatedCustomerAccount -> {
                    merchantAccount.setBalance(merchantAccount.getBalance().add(requestDTO.getAmount()));
                    return accountService.update(merchantAccount)
                            .flatMap(updatedMerchantAccount -> {
                                Transaction transaction = createTransactionRecord(updatedCustomerAccount, updatedMerchantAccount, requestDTO);
                                return Mono.just(transaction);
                            });
                });
    }

    private Transaction createTransactionRecord(Account customerAccount, Account merchantAccount, RequestDTO requestDTO) {
        Transaction transaction = new Transaction();
        transaction.setCustomerAccountId(customerAccount.getId());
        transaction.setMerchantAccountId(merchantAccount.getId());
        transaction.setOperationType(OperationType.TOP_UP);
        transaction.setAmount(requestDTO.getAmount());
        transaction.setCurrency(customerAccount.getCurrency());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.IN_PROGRESS);
        transaction.setLanguage(requestDTO.getLanguage());
        transaction.setMessage("OK");
        transaction.setNotificationUrl(requestDTO.getNotificationUrl());
        transaction.setPaymentMethod(requestDTO.getPaymentMethod());
        return transaction;
    }

    private Card createCard(Account account, RequestDTO requestDTO) {
        Card card = new Card();
        card.setAccountId(account.getId());
        card.setCardNumber(requestDTO.getCardNumber());
        card.setExpDate(requestDTO.getExpDate());
        card.setCvv(requestDTO.getCvv());
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setStatus(Status.ACTIVE);
        return card;
    }

    private Customer createCustomer(RequestDTO requestDTO) {
        Customer customer = new Customer();
        customer.setFirstname(requestDTO.getFirstName());
        customer.setLastname(requestDTO.getLastName());
        customer.setCountry(requestDTO.getCountry());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setStatus(Status.ACTIVE);
        return customer;
    }

    private Account createAccount(Customer customer, String currency) {
        Account account = new Account();
        account.setOwnerType("customer");
        account.setCurrency(currency);
        account.setCustomerId(customer.getId());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        account.setStatus(Status.ACTIVE);
        return account;
    }
}