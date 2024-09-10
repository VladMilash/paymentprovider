package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.*;
import com.mvo.paymentprovider.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Log4j2
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
    public Mono<Transaction> createTransaction(String paymentMethod, BigDecimal amount, String currency,
                                               Long cardNumber, String expDate, String cvv,
                                               String language, String notificationUrl, String firstName,
                                               String lastName, String country, UUID merchantId) {
        log.info("Creating transaction: paymentMethod {}, amount {}, currency {}, merchantId {}", paymentMethod, amount, currency, merchantId);

        return merchantService.findById(merchantId)
                .switchIfEmpty(Mono.error(new RuntimeException("Merchant not found")))
                .flatMap(merchant -> {
                    if (!merchant.getStatus().equals(Status.ACTIVE)) {
                        log.warn("Merchant with id {} is not active", merchantId);
                        return Mono.error(new RuntimeException("Merchant is not active"));
                    }
                    log.info("Merchant with id {} found and is active", merchantId);

                    return accountService.findByMerchantIdAndCurrency(merchantId, currency)
                            .switchIfEmpty(Mono.error(new RuntimeException("Merchant account not found")))
                            .flatMap(merchantAccount -> {
                                log.info("Merchant account for merchantId {} and currency {} found", merchantId, currency);

                                return customerService.findByFirstnameAndLastnameAndCountry(firstName, lastName, country)
                                        .switchIfEmpty(Mono.defer(() -> {
                                            log.info("Customer not found, creating new customer: firstName {}, lastName {}, country {}", firstName, lastName, country);
                                            return customerService.createCustomer(createCustomer(firstName, lastName, country));
                                        }))
                                        .flatMap(customer -> {
                                            log.info("Customer with name {} {} found/created", firstName, lastName);

                                            return accountService.findByCustomerIdAndCurrency(customer.getId(), currency)
                                                    .switchIfEmpty(Mono.defer(() -> {
                                                        log.info("Customer account not found, creating new account for customerId {} and currency {}", customer.getId(), currency);
                                                        return accountService.createAccount(createAccount(customer, currency));
                                                    }))
                                                    .flatMap(customerAccount -> {
                                                        log.info("Customer account found/created for customerId {} and currency {}", customer.getId(), currency);

                                                        return cardService.findByCardNumber(cardNumber)
                                                                .switchIfEmpty(Mono.defer(() -> {
                                                                    log.info("Card not found, creating new card for cardNumber {}", cardNumber);
                                                                    return cardService.createCard(createCard(customerAccount, cardNumber, expDate, cvv));
                                                                }))
                                                                .flatMap(card -> {
                                                                    log.info("Card with cardNumber {} found/created", cardNumber);

                                                                    return topUpMerchantAccount(customerAccount, merchantAccount, amount, language, notificationUrl, paymentMethod)
                                                                            .flatMap(transaction -> {
                                                                                log.info("Transaction for amount {} and paymentMethod {} is in progress", amount, paymentMethod);
                                                                                transaction.setTransactionStatus(TransactionStatus.IN_PROGRESS);

                                                                                return transactionRepository.save(transaction)
                                                                                        .flatMap(savedTransaction -> {
                                                                                            log.info("Transaction with id {} successfully saved", savedTransaction.getId());
                                                                                            webhookService.sendNotification(transaction);
                                                                                            return Mono.just(savedTransaction);
                                                                                        });
                                                                            });
                                                                });
                                                    });
                                        });
                            });
                })
                .doOnError(error -> log.error("Failed to create transaction: {}", error.getMessage(), error));
    }


    @Override
    public Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus) {
        return transactionRepository.findById(transactionId)
                .map(transaction -> {
                    transaction.setTransactionStatus(newStatus);
                    return transaction;
                })
                .flatMap(transactionRepository::save)
                .doOnSuccess(transaction -> log.info("Transaction status with id {} has been updated successfully< new status {}", transactionId, newStatus))
                .doOnError(error -> log.error("Failed to update transaction status transaction with id {}", transactionId, error));

    }

    @Override
    public Flux<Transaction> getTransactionsByDay(LocalDate date) {
        return null;
    }

    @Override
    public Flux<Transaction> getTransactionsByPeriod(LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public Mono<Transaction> getTransactionDetails(UUID transactionId) {
        return null;
    }

    private Mono<Transaction> topUpMerchantAccount(Account customerAccount, Account merchantAccount, BigDecimal amount, String language, String notificationUrl, String paymentMethod) {
        customerAccount.setBalance(customerAccount.getBalance().subtract(amount));
        return accountService.update(customerAccount)
                .flatMap(updatedCustomerAccount -> {
                    merchantAccount.setBalance(merchantAccount.getBalance().add(amount));
                    return accountService.update(merchantAccount)
                            .flatMap(updatedMerchantAccount -> {
                                Transaction transaction = createTransactionRecord(updatedCustomerAccount, updatedMerchantAccount, amount, language, notificationUrl, paymentMethod);
                                return transactionRepository.save(transaction);
                            });
                });
    }


    private Transaction createTransactionRecord(Account customerAccount, Account merchantAccount, BigDecimal amount, String language, String notificationUrl,
                                                String paymentMethod) {
        Transaction transaction = new Transaction();
        transaction.setCustomerAccountId(customerAccount.getId());
        transaction.setMerchantAccountId(merchantAccount.getId());
        transaction.setOperationType(OperationType.TOP_UP);
        transaction.setAmount(amount);
        transaction.setCurrency(customerAccount.getCurrency());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.IN_PROGRESS);
        transaction.setLanguage(language);
        transaction.setMessage("OK");
        transaction.setNotificationUrl(notificationUrl);
        transaction.setPaymentMethod(paymentMethod);
        return transaction;
    }


    private Card createCard(Account account, Long cardNumber, String expDate, String cvv) {
        Card card = new Card();
        card.setAccountId(account.getId());
        card.setCardNumber(cardNumber);
        card.setExpDate(expDate);
        card.setCvv(cvv);
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setStatus(Status.ACTIVE);
        return card;
    }

    private Customer createCustomer(String firstname, String lastname, String country) {
        Customer customer = new Customer();
        customer.setFirstname(firstname);
        customer.setLastname(lastname);
        customer.setCountry(country);
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
