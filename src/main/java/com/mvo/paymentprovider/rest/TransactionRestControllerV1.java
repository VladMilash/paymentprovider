package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.CardDTO;
import com.mvo.paymentprovider.dto.CustomerDTO;
import com.mvo.paymentprovider.dto.MerchantDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/transactions/")
public class TransactionRestControllerV1 {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping
    public Mono<ResponseEntity<TransactionDTO>> topUp(@RequestBody TransactionDTO transactionDTO,
                                                      @RequestBody CardDTO cardDTO,
                                                      @RequestBody CustomerDTO customerDTO,
                                                      @AuthenticationPrincipal MerchantDTO merchantDTO) {

        return transactionService.createTransaction(transactionDTO, cardDTO, customerDTO, merchantDTO)
                .map(transaction -> ResponseEntity.status(HttpStatus.OK)
                        .body(createReturnedTransactionDTO(transaction)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(TransactionDTO.builder().transactionStatus(TransactionStatus.FAILED).message(e.getMessage()).build())));


    }

    @GetMapping
    public Flux<TransactionDTO> getTransactionsByCurrentDay() {
        return transactionService.getTransactionsByCreatedAtBetween(LocalDate.now(), LocalDate.now())
                .map(transactionMapper::map);

    }

    @GetMapping
    public Flux<TransactionDTO> getTransactionByPeriod(@RequestParam("start_date") LocalDate startDate,
                                                       @RequestParam("end_date") LocalDate endDate) {
        return transactionService.getTransactionsByCreatedAtBetween(startDate, endDate)
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
