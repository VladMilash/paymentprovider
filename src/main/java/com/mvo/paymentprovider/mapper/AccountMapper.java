package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.AccountDTO;
import com.mvo.paymentprovider.entity.Account;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDTO map(Account account);

    @InheritInverseConfiguration
    Account map(AccountDTO accountDTO);
}
