package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.dto.CardDTO;
import com.mvo.paymentprovider.dto.CustomerDTO;
import com.mvo.paymentprovider.dto.MerchantDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.*;
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
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CardService cardService;
    private final MerchantService merchantService;
    private final CustomerService customerService;
    private final WebhookService webhookService;

    @Override
    @Transactional
    public Mono<Transaction> createTransaction(TransactionDTO transactionDTO, CardDTO cardDTO,
                                               CustomerDTO customerDTO, MerchantDTO merchantDTO) {
        log.info("Creating transaction: paymentMethod {}, amount {}, currency {}, merchantId {}",
                transactionDTO.getPaymentMethod(), transactionDTO.getAmount(), transactionDTO.getCurrency(), merchantDTO.getId());

        return merchantService.findById(merchantDTO.getId())
                .switchIfEmpty(Mono.error(new RuntimeException("Merchant not found")))
                .flatMap(merchant -> accountService.findByMerchantIdAndCurrency(merchantDTO.getId(), transactionDTO.getCurrency())
                        .switchIfEmpty(Mono.error(new RuntimeException("Merchant account not found")))
                        .flatMap(merchantAccount -> {
                            log.info("Merchant account for merchantId {} and currency {} found",
                                    merchantDTO.getId(), transactionDTO.getCurrency());

                            return customerService.findByFirstnameAndLastnameAndCountry(customerDTO.getFirstname(),
                                            customerDTO.getLastname(), customerDTO.getCountry())
                                    .switchIfEmpty(Mono.defer(() -> {
                                        log.info("Customer not found, creating new customer: firstName {}, lastName {}, country {}",
                                                customerDTO.getFirstname(), customerDTO.getLastname(), customerDTO.getCountry());
                                        return customerService.createCustomer(createCustomer(customerDTO));
                                    }))
                                    .flatMap(customer -> {
                                        log.info("Customer with name {} {} found/created",
                                                customerDTO.getFirstname(), customerDTO.getLastname());

                                        return accountService.findByCustomerIdAndCurrency(customer.getId(), transactionDTO.getCurrency())
                                                .switchIfEmpty(Mono.defer(() -> {
                                                    log.info("Customer account not found, creating new account for customerId {} and currency {}",
                                                            customer.getId(), transactionDTO.getCurrency());
                                                    return accountService.createAccount(createAccount(customer, transactionDTO.getCurrency()));
                                                }))
                                                .flatMap(customerAccount -> {
                                                    log.info("Customer account found/created for customerId {} and currency {}",
                                                            customer.getId(), transactionDTO.getCurrency());

                                                    return cardService.findByCardNumber(cardDTO.getCardNumber())
                                                            .switchIfEmpty(Mono.defer(() -> {
                                                                log.info("Card not found, creating new card for cardNumber {}", cardDTO.getCardNumber());
                                                                return cardService.createCard(createCard(customerAccount, cardDTO));
                                                            }))
                                                            .flatMap(card -> {
                                                                log.info("Card with cardNumber {} found/created", cardDTO.getCardNumber());

                                                                return topUpMerchantAccount(customerAccount, merchantAccount, transactionDTO)
                                                                        .flatMap(transaction -> {
                                                                            log.info("Transaction for amount {} and paymentMethod {} is in progress",
                                                                                    transactionDTO.getAmount(), transactionDTO.getPaymentMethod());

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
    public Flux<Transaction> getTransactionsByCreatedAtBetween(LocalDate startDate, LocalDate endDate) {
        OperationType operationType = OperationType.TOP_UP;
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return transactionRepository.getTransactionsByCreatedAtBetweenAndOperationType(start, end, operationType)
                .doOnNext(transaction -> log.info("Transactions by period {} {} have been found successfully", startDate, endDate))
                .doOnError(error -> log.error("Failed to find transactions by period {} {}", startDate, endDate, error));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Transaction> getTransactionDetails(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .doOnSuccess(transaction -> log.info("Transaction with id {} has been found successfully", transactionId))
                .doOnError(error -> log.error("Failed to find transaction with id {}", transactionId, error));
    }

    private Mono<Transaction> topUpMerchantAccount(Account customerAccount, Account merchantAccount, TransactionDTO transactionDTO) {
        customerAccount.setBalance(customerAccount.getBalance().subtract(transactionDTO.getAmount()));
        return accountService.update(customerAccount)
                .flatMap(updatedCustomerAccount -> {
                    merchantAccount.setBalance(merchantAccount.getBalance().add(transactionDTO.getAmount()));
                    return accountService.update(merchantAccount)
                            .flatMap(updatedMerchantAccount -> {
                                Transaction transaction = createTransactionRecord(updatedCustomerAccount, updatedMerchantAccount, transactionDTO);
                                return transactionRepository.save(transaction);
                            });
                });
    }

    private Transaction createTransactionRecord(Account customerAccount, Account merchantAccount,
                                                TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setCustomerAccountId(customerAccount.getId());
        transaction.setMerchantAccountId(merchantAccount.getId());
        transaction.setOperationType(OperationType.TOP_UP);
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setCurrency(customerAccount.getCurrency());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.IN_PROGRESS);
        transaction.setLanguage(transactionDTO.getLanguage());
        transaction.setMessage("OK");
        transaction.setNotificationUrl(transactionDTO.getNotificationUrl());
        transaction.setPaymentMethod(transactionDTO.getPaymentMethod());
        return transaction;
    }

    private Card createCard(Account account, CardDTO cardDTO) {
        Card card = new Card();
        card.setAccountId(account.getId());
        card.setCardNumber(cardDTO.getCardNumber());
        card.setExpDate(cardDTO.getExpDate());
        card.setCvv(cardDTO.getCvv());
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setStatus(Status.ACTIVE);
        return card;
    }

    private Customer createCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setFirstname(customerDTO.getFirstname());
        customer.setLastname(customerDTO.getLastname());
        customer.setCountry(customerDTO.getCountry());
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