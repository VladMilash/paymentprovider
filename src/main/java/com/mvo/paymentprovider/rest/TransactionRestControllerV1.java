package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.*;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.security.MerchantDetails;
import com.mvo.paymentprovider.service.TransactionService;
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
@RequestMapping("api/v1/transactions/")
public class TransactionRestControllerV1 {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping
    public Mono<ResponseEntity<TransactionDTO>> topUp(@AuthenticationPrincipal MerchantDetails merchantDetails,
                                                      @RequestBody RequestDTO requestDTO) {
        requestDTO.setMerchantId(merchantDetails.getMerchant().getId());
        return transactionService.createTransaction(requestDTO)
                .map(transaction -> ResponseEntity.status(HttpStatus.OK)
                        .body(createReturnedTransactionDTO(transaction)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(TransactionDTO.builder().transactionStatus(TransactionStatus.FAILED).message(e.getMessage()).build())));
    }

    @GetMapping
    public Flux<TransactionDTO> getTransactions(
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

        return transactionService.getTransactionsByCreatedAtBetween(start, end)
                .map(transactionMapper::map);
    }

    @GetMapping("/{transactionId}")
    public Mono<TransactionDTO> getTransactionDetails(@PathVariable("transactionId") UUID id) {
        return transactionService.getTransactionDetails(id)
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
