package com.mvo.paymentprovider.rest;

import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.mapper.TransactionMapper;
import com.mvo.paymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/transactions/")
public class TransactionRestControllerV1 {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping
    public Mono<ResponseEntity<TransactionDTO>> topUp(TransactionDTO transactionDTO) {
        return  null;
    }
}
