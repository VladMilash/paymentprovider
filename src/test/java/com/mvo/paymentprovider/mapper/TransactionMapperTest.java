package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class TransactionMapperTest {
    private Transaction testTransaction;
    private TransactionMapper transactionMapper = new TransactionMapperImpl();


    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionStatus(TransactionStatus.SUCCESS)
                .message("Ok")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .paymentMethod("CARD")
                .amount(new BigDecimal(100))
                .operationType(OperationType.TOP_UP)
                .notificationUrl("test")
                .merchantAccountId(UUID.randomUUID())
                .currency("USD")
                .language("Eng")
                .build();
    }

    @Test
    void map() {
        TransactionDTO testTransactionDTO = transactionMapper.map(testTransaction);

        assertEquals(testTransactionDTO.getId(), testTransaction.getId());
    }

    @Test
    void testMap() {
    }
}