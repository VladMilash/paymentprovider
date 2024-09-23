package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.RequestDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.Merchant;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.security.MerchantDetails;
import com.mvo.paymentprovider.service.PayoutService;
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
class PayoutRestControllerV1Test {
    @Mock
    private PayoutService payoutService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private PayoutRestControllerV1 controller;

    private MerchantDetails merchantDetails;
    private Transaction transaction;
    private TransactionDTO transactionDTO;
    private RequestDTO requestDTO;
    private Merchant merchant;

    UUID merchantId;
    UUID transactionId;

    @BeforeEach
    void setUp() {
       merchantId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        String secretKey = "1212";

        merchant = Merchant.builder()
                .id(merchantId)
                .secretKey(secretKey)
                .build();

        merchantDetails = new MerchantDetails(merchant);

        transaction = Transaction.builder()
                .id(transactionId)
                .transactionStatus(TransactionStatus.SUCCESS)
                .message("Ok")
                .build();

        transactionDTO = TransactionDTO.builder()
                .id(transactionId)
                .transactionStatus(TransactionStatus.SUCCESS)
                .message("Ok")
                .build();

        requestDTO = RequestDTO.builder()
                .merchantId(merchantId)
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
    void createPayout() {
        Mockito.when(payoutService.createPayout(any(RequestDTO.class)))
                .thenReturn(Mono.just(transaction));

        Mono<ResponseEntity<TransactionDTO>> result = controller.createPayout(merchantDetails, requestDTO);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().getId().equals(transaction.getId()) &&
                                responseEntity.getBody().getTransactionStatus() == transaction.getTransactionStatus() &&
                                responseEntity.getBody().getMessage().equals(transaction.getMessage())
                )
                .verifyComplete();

        verify(payoutService, times(1))
                .createPayout(any(RequestDTO.class));
    }

    @Test
    void getPayouts() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Mockito.when(payoutService.getPayoutsByCreatedAtBetween(any(LocalDate.class), any(LocalDate.class), any(UUID.class)))
                .thenReturn(Flux.just(transaction));

        Mockito.when(transactionMapper.map(any(Transaction.class)))
                .thenReturn(transactionDTO);

        Flux<TransactionDTO> result = controller.getPayouts(
                startDate.toEpochDay(), endDate.toEpochDay(), merchantDetails);

        StepVerifier.create(result)
                .expectNext(transactionDTO)
                .verifyComplete();

        verify(payoutService, times(1))
                .getPayoutsByCreatedAtBetween(any(LocalDate.class), any(LocalDate.class), any(UUID.class));
        verify(transactionMapper, times(1))
                .map(any(Transaction.class));
    }

    @Test
    void getPayoutDetails() {
        Mockito.when(payoutService.getPayoutDetails(transactionId))
                .thenReturn(Mono.just(transaction));

        Mockito.when(transactionMapper.map(transaction))
                .thenReturn(transactionDTO);

        Mono<TransactionDTO> result = controller.getPayoutDetails(transactionId);

        StepVerifier.create(result)
                .expectNext(transactionDTO)
                .verifyComplete();

        verify(payoutService, times(1)).getPayoutDetails(transactionId);
        verify(transactionMapper, times(1)).map(transaction);
    }
}