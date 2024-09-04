package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.Transaction;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionDTO map(Transaction transaction);

    @InheritInverseConfiguration
    Transaction map(TransactionDTO transactionDTO);
}
