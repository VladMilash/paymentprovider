package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.CardDTO;
import com.mvo.paymentprovider.dto.CustomerDTO;
import com.mvo.paymentprovider.dto.MerchantDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/payouts/")
public class PayoutRestControllerV1 {
    private final PayoutService payoutService;
    private final TransactionMapper transactionMapper;

    @PostMapping
    public Mono<ResponseEntity<TransactionDTO>> createPayout(@RequestBody TransactionDTO transactionDTO,
                                                             @RequestBody CardDTO cardDTO,
                                                             @RequestBody CustomerDTO customerDTO,
                                                             @AuthenticationPrincipal MerchantDTO merchantDTO) {
        return payoutService.createPayout(transactionDTO, cardDTO, customerDTO, merchantDTO)
                .map(transaction -> ResponseEntity.status(HttpStatus.OK)
                        .body(createReturnedTransactionDTO(transaction)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(TransactionDTO.builder().transactionStatus(TransactionStatus.FAILED).message(e.getMessage()).build())));
    }

    @GetMapping
    public Flux<TransactionDTO> getPayouts(
            @RequestParam(value = "start_date", required = false) Long startDate,
            @RequestParam(value = "end_date", required = false) Long endDate) {

        LocalDate start;
        LocalDate end;

        if (startDate == null || endDate == null) {
            start = LocalDate.now();
            end = LocalDate.now();
        } else {
            start = Instant.ofEpochSecond(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
            end = Instant.ofEpochSecond(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
        }

        return payoutService.getPayoutsByCreatedAtBetween(start, end)
                .map(transactionMapper::map);
    }

    @GetMapping("/{payoutId}")
    public Mono<TransactionDTO> getTransactionDetails(@PathVariable("payoutId") UUID id) {
        return payoutService.getPayoutDetails(id)
                .map(transactionMapper::map);
    }

    private TransactionDTO createReturnedTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .transactionStatus(transaction.getTransactionStatus())
                .message(transaction.getMessage())
                .build();
    }
}
