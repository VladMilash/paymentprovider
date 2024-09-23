package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.*;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.mapper.CustomTransactionMapper;
import com.mvo.paymentprovider.security.MerchantDetails;
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
import java.time.ZoneOffset;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/payouts/")
public class PayoutRestControllerV1 {
    private final PayoutService payoutService;
    private final CustomTransactionMapper transactionMapper;

    @PostMapping
    public Mono<ResponseEntity<TransactionDTO>> createPayout(@AuthenticationPrincipal MerchantDetails merchantDetails,
                                                             @RequestBody RequestDTO requestDTO) {
        requestDTO.setMerchantId(merchantDetails.getMerchant().getId());
        return payoutService.createPayout(requestDTO)
                .map(transaction -> ResponseEntity.status(HttpStatus.OK)
                        .body(createReturnedTransactionDTO(transaction)));
    }

    @GetMapping
    public Flux<TransactionDTO> getPayouts(
            @RequestParam(value = "start_date", required = false) Long startDate,
            @RequestParam(value = "end_date", required = false) Long endDate,
            @AuthenticationPrincipal MerchantDetails merchantDetails) {

        LocalDate start;
        LocalDate end;

        if (startDate == null || endDate == null) {
            start = LocalDate.now(ZoneOffset.UTC);
            end = LocalDate.now(ZoneOffset.UTC);
        } else {
            start = Instant.ofEpochSecond(startDate).atZone(ZoneOffset.UTC).toLocalDate();
            end = Instant.ofEpochSecond(endDate).atZone(ZoneOffset.UTC).toLocalDate();
        }

        return payoutService.getPayoutsByCreatedAtBetween(start, end, merchantDetails.getMerchant().getId())
                .map(transactionMapper::map);
    }

    @GetMapping("/{payoutId}")
    public Mono<TransactionDTO> getPayoutDetails(@PathVariable("payoutId") UUID id) {
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
