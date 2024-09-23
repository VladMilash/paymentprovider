package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class CustomTransactionMapper implements TransactionMapper {
    @Override
    public TransactionDTO map(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionDTO.builder()
                .id(transaction.getId())
                .transactionStatus(transaction.getTransactionStatus())
                .message(transaction.getMessage())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .paymentMethod(transaction.getPaymentMethod())
                .amount(transaction.getAmount())
                .operationType(transaction.getOperationType())
                .notificationUrl(transaction.getNotificationUrl())
                .merchantAccountId(transaction.getMerchantAccountId())
                .customerAccountId(transaction.getCustomerAccountId())
                .currency(transaction.getCurrency())
                .language(transaction.getLanguage())
                .build();
    }

    @Override
    public Transaction map(TransactionDTO transactionDTO) {
        if (transactionDTO == null) {
            return null;
        }

        return Transaction.builder()
                .id(transactionDTO.getId())
                .transactionStatus(transactionDTO.getTransactionStatus())
                .message(transactionDTO.getMessage())
                .createdAt(transactionDTO.getCreatedAt())
                .updatedAt(transactionDTO.getUpdatedAt())
                .paymentMethod(transactionDTO.getPaymentMethod())
                .amount(transactionDTO.getAmount())
                .operationType(transactionDTO.getOperationType())
                .notificationUrl(transactionDTO.getNotificationUrl())
                .merchantAccountId(transactionDTO.getMerchantAccountId())
                .customerAccountId(transactionDTO.getCustomerAccountId())
                .currency(transactionDTO.getCurrency())
                .language(transactionDTO.getLanguage())
                .build();
    }
}
