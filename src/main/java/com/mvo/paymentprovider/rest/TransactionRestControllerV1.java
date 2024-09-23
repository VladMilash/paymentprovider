package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.*;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.mapper.CustomTransactionMapper;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.security.MerchantDetails;
import com.mvo.paymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("api/v1/transactions/")
public class TransactionRestControllerV1 {
    private final TransactionService transactionService;
    private final CustomTransactionMapper transactionMapper;

    @PostMapping
    public Mono<ResponseEntity<TransactionDTO>> topUp(@AuthenticationPrincipal MerchantDetails merchantDetails,
                                                      @RequestBody RequestDTO requestDTO) {
        requestDTO.setMerchantId(merchantDetails.getMerchant().getId());
        return transactionService.createTransaction(requestDTO)
                .map(transaction -> ResponseEntity.status(HttpStatus.OK)
                        .body(createReturnedTransactionDTO(transaction)));
    }

    @GetMapping
    public Flux<TransactionDTO> getTransactions(
            @RequestParam(value = "start_date", required = false) Long startDate,
            @RequestParam(value = "end_date", required = false) Long endDate,
            @AuthenticationPrincipal MerchantDetails merchantDetails) {

        LocalDate start;
        LocalDate end;

        log.info("Received request with start_date: {}, end_date: {}", startDate, endDate);

        if (startDate == null || endDate == null) {
            start = LocalDate.now(ZoneOffset.UTC);
            end = LocalDate.now(ZoneOffset.UTC);
            log.info("No start_date or end_date provided, using current date: {}", start);
        } else {
            start = Instant.ofEpochSecond(startDate).atZone(ZoneOffset.UTC).toLocalDate();
            end = Instant.ofEpochSecond(endDate).atZone(ZoneOffset.UTC).toLocalDate();
            log.info("Parsed dates from request: start = {}, end = {}", start, end);
        }

        UUID merchantId = merchantDetails.getMerchant().getId();
        log.info("Merchant ID: {}", merchantId);

        return transactionService.getTransactionsByCreatedAtBetween(start, end, merchantId)
                .doOnNext(transaction -> log.info("Found transaction: {}", transaction))
                .doOnComplete(() -> log.info("Transaction retrieval completed."))
                .doOnError(error -> log.error("Error retrieving transactions: ", error))
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