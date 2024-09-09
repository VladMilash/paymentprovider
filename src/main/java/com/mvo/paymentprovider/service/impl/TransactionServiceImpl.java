package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.*;
import com.mvo.paymentprovider.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
        return merchantService.findById(merchantId)
                .switchIfEmpty(Mono.error(new RuntimeException("Merchant not found")))
                .flatMap(merchant -> {
                    if (!merchant.getStatus().equals(Status.ACTIVE)) {
                        return Mono.error(new RuntimeException("Merchant is not active"));
                    }
                    return accountService.findByMerchantIdAndCurrency(merchantId,currency)
                            .switchIfEmpty(Mono.error(new RuntimeException("Merchant account not found")))
                            .flatMap(merchantAccount ->
                                            customerService.findByFirstnameAndLastnameAndCountry(firstName, lastName, country)
                                                    .switchIfEmpty(customerService.createCustomer(createCustomer(firstName,lastName,country)))
                                                    .flatMap(customer ->
                                                            accountService.findByCustomerIdAndCurrency(customer.getId(), currency)
                                                                    .switchIfEmpty(accountService.createAccount())
                                                            )


                                    )
                })
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

    private Account createAccount(Customer customer, String currency){
        Account account = new Account();
        account.setOwnerType("customer");
        account.setCurrency(currency);
        account.setCustomerId(customer.getId());
        account.setCards();
    }

    @Override
    public Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus) {
        return null;
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

}
