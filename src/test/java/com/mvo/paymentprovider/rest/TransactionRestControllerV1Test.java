package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.RequestDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.Merchant;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import com.mvo.paymentprovider.mapper.CustomTransactionMapper;
import com.mvo.paymentprovider.security.MerchantDetails;
import com.mvo.paymentprovider.service.TransactionService;
import com.mvo.paymentprovider.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionRestControllerV1Test {
    @Mock
    private TransactionService transactionService;

    @Mock
    private CustomTransactionMapper transactionMapper;

    @InjectMocks
    private TransactionRestControllerV1 controller;

    private MerchantDetails merchantDetails;
    private Transaction transaction;
    private TransactionDTO transactionDTO;
    private RequestDTO requestDTO;
    private Merchant merchant;


    @BeforeEach
    void setUp() {
        String secretKey = "1212";

        merchant = DataUtils.getPersistedMerchant();
        merchant.setSecretKey(secretKey);

        merchantDetails = new MerchantDetails(merchant);

        transaction = DataUtils.getPersistedTransactionPayout();
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setMessage("Ok");

        transactionDTO = TransactionDTO.builder()
                .id(transaction.getId())
                .transactionStatus(TransactionStatus.SUCCESS)
                .message("Ok")
                .build();

        requestDTO = RequestDTO.builder()
                .merchantId(merchant.getId())
                .amount(new BigDecimal("12.00"))
                .currency("USD")
                .firstName("John")
                .lastName("Doe")
                .country("US")
                .cardNumber(1L)
                .paymentMethod("CARD")
                .build();
    }

    @Test
    void topUp() {
        Mockito.when(transactionService.createTransaction(any(RequestDTO.class)))
                .thenReturn(Mono.just(transaction));

        Mono<ResponseEntity<TransactionDTO>> result = controller.topUp(merchantDetails, requestDTO);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().getId().equals(transaction.getId()) &&
                                responseEntity.getBody().getTransactionStatus() == transaction.getTransactionStatus() &&
                                responseEntity.getBody().getMessage().equals(transaction.getMessage())
                )
                .verifyComplete();

        verify(transactionService, times(1))
                .createTransaction(any(RequestDTO.class));
    }

    @Test
    void getTransactions() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Mockito.when(transactionService.getTransactionsByCreatedAtBetween(any(LocalDate.class),
                        any(LocalDate.class), any(UUID.class)))
                .thenReturn(Flux.just(transaction));

        Mockito.when(transactionMapper.map(any(Transaction.class)))
                .thenReturn(transactionDTO);

        Flux<TransactionDTO> result = controller.getTransactions(
                startDate.toEpochDay(), endDate.toEpochDay(), merchantDetails);

        StepVerifier.create(result)
                .expectNext(transactionDTO)
                .verifyComplete();

        verify(transactionService, times(1))
                .getTransactionsByCreatedAtBetween(any(LocalDate.class), any(LocalDate.class), any(UUID.class));
        verify(transactionMapper, times(1))
                .map(any(Transaction.class));
    }

    @Test
    void getTransactionDetails() {
        Mockito.when(transactionService.getTransactionDetails(transaction.getId()))
                .thenReturn(Mono.just(transaction));

        Mockito.when(transactionMapper.map(transaction))
                .thenReturn(transactionDTO);

        Mono<TransactionDTO> result = controller.getTransactionDetails(transaction.getId());

        StepVerifier.create(result)
                .expectNext(transactionDTO)
                .verifyComplete();

        verify(transactionService, times(1)).getTransactionDetails(transaction.getId());
        verify(transactionMapper, times(1)).map(transaction);
    }
}