package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.dto.RequestDTO;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.TransactionRepository;
import com.mvo.paymentprovider.service.AccountService;
import com.mvo.paymentprovider.service.CustomerService;
import com.mvo.paymentprovider.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PayoutServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private MerchantService merchantService;
    @Mock
    private CustomerService customerService;
    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private PayoutServiceImpl payoutService;

    private Transaction transaction;
    private Account merchantAccount;
    private Account customerAccount;
    private Merchant merchant;
    private Customer customer;
    private RequestDTO requestDTO;

    private final UUID transactionId = UUID.fromString("12122122-212b-4077-af84-694a0e69b8e1");
    private final UUID merchantId = UUID.fromString("22222222-212b-4077-af84-694a0e69b8e1");
    private final UUID customerId = UUID.fromString("33333333-212b-4077-af84-694a0e69b8e1");

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .id(transactionId)
                .operationType(OperationType.PAYOUT)
                .build();

        merchantAccount = Account.builder()
                .id(UUID.randomUUID())
                .merchantId(merchantId)
                .currency("USD")
                .balance(new BigDecimal("1000.00"))
                .build();

        customerAccount = Account.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .currency("USD")
                .balance(new BigDecimal("100.00"))
                .build();

        merchant = Merchant.builder()
                .id(merchantId)
                .build();

        customer = Customer.builder()
                .id(customerId)
                .firstname("John")
                .lastname("Doe")
                .country("US")
                .build();

        requestDTO = RequestDTO.builder()
                .merchantId(merchantId)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .firstName("John")
                .lastName("Doe")
                .country("US")
                .paymentMethod("CARD")
                .build();
    }

    @Test
    void createPayout() {
        Mockito.when(merchantService.findById(any(UUID.class)))
                .thenReturn(Mono.just(merchant));

        Mockito.when(accountService.findByMerchantIdAndCurrency(any(UUID.class), any(String.class)))
                .thenReturn(Mono.just(merchantAccount));

        Mockito.when(customerService.findByFirstnameAndLastnameAndCountry(any(String.class), any(String.class),
                        any(String.class)))
                .thenReturn(Mono.just(customer));

        Mockito.when(accountService.findByCustomerIdAndCurrency(any(UUID.class), any(String.class)))
                .thenReturn(Mono.just(customerAccount));

        Mockito.when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Mono.just(transaction));

        Mockito.when(webhookService.sendNotification(any(Transaction.class)))
                .thenReturn(Mono.empty());

        Mockito.when(accountService.update(any(Account.class)))
                .thenReturn(Mono.just(new Account()));

        Mono<Transaction> transactionMono = payoutService.createPayout(requestDTO);

        StepVerifier.create(transactionMono)
                .expectNext(transaction)
                .verifyComplete();

        Mockito.verify(merchantService, Mockito.times(1))
                .findById(any(UUID.class));

        Mockito.verify(accountService, Mockito.times(1))
                .findByMerchantIdAndCurrency(any(UUID.class), any(String.class));

        Mockito.verify(customerService, Mockito.times(1))
                .findByFirstnameAndLastnameAndCountry(any(String.class), any(String.class), any(String.class));

        Mockito.verify(accountService, Mockito.times(1))
                .findByCustomerIdAndCurrency(any(UUID.class), any(String.class));

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(any(Transaction.class));

        Mockito.verify(webhookService, Mockito.times(1))
                .sendNotification(any(Transaction.class));

        Mockito.verify(accountService, Mockito.times(2))
                .update(any(Account.class));
    }

    @Test
    void getPayoutsByCreatedAtBetween() {
        LocalDate startDate = LocalDate.of(2023, 9, 1);
        LocalDate endDate = LocalDate.of(2023, 9, 30);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Mockito.when(accountService.findByMerchantId(any(UUID.class)))
                .thenReturn(Mono.just(merchantAccount));

        Mockito.when(transactionRepository.getTransactionsByCreatedAtBetweenAndOperationTypeAndMerchantAccountId(
                        any(LocalDateTime.class), any(LocalDateTime.class), any(OperationType.class), any(UUID.class)))
                .thenReturn(Flux.fromIterable(transactions));

        StepVerifier.create(payoutService.getPayoutsByCreatedAtBetween(startDate, endDate, merchantId))
                .expectNextMatches(returnedTransaction ->
                        returnedTransaction.getId().equals(transaction.getId())
                )
                .verifyComplete();

        verify(accountService, Mockito.times(1)).findByMerchantId(any(UUID.class));
        verify(transactionRepository, Mockito.times(1))
                .getTransactionsByCreatedAtBetweenAndOperationTypeAndMerchantAccountId(any(LocalDateTime.class),
                        any(LocalDateTime.class), any(OperationType.class), any(UUID.class));
    }

    @Test
    void getPayoutDetails() {
        Mockito.when(transactionRepository.findById(transactionId))
                .thenReturn(Mono.just(transaction));

        Mono<Transaction> transactionMono = payoutService.getPayoutDetails(transactionId);

        StepVerifier.create(transactionMono)
                .expectNext(transaction)
                .verifyComplete();

        Mockito.verify(transactionRepository, Mockito.times(1)).findById(transactionId);
    }
}