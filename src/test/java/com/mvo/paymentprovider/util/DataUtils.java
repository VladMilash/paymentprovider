package com.mvo.paymentprovider.util;

import com.mvo.paymentprovider.entity.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public class DataUtils {
    private static final String TEST_PASSWORD = "1";

    public static Merchant getTransientMerchant() {
        return Merchant.builder()
                .name("MerchantTest")
                .secretKey(Base64.getEncoder().encodeToString(TEST_PASSWORD.getBytes()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
    }

    public static Customer getTransientCustomer() {
        return Customer.builder()
                .firstname("John")
                .lastname("Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .country("USA")
                .build();
    }

    public static Account getTransientAccountWithoutBalanceAndOwnerTypeAndOwnerId() {
        return Account.builder()
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
    }

    public static Card getTransientCard() {
        return Card.builder()
                .cardNumber(1234567890123456L)
                .expDate("12/25")
                .cvv("123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
    }

    public static Transaction getPersistedTransactionPayout() {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .operationType(OperationType.PAYOUT)
                .build();
    }

    public static Transaction getPersistedTransactionTopUp() {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .operationType(OperationType.TOP_UP)
                .build();
    }

    public static Account getPersistedAccount() {
        return Account.builder()
                .id(UUID.randomUUID())
                .currency("USD")
                .build();
    }

    public static Merchant getPersistedMerchant() {
        return Merchant.builder()
                .id(UUID.randomUUID())
                .build();
    }

    public static Customer getPersistedCustomer() {
        return Customer.builder()
                .id(UUID.randomUUID())
                .firstname("John")
                .lastname("Doe")
                .country("US")
                .build();
    }

    public static Card getPersistedCard() {
        return Card.builder()
                .cardNumber(1L)
                .build();
    }
}
