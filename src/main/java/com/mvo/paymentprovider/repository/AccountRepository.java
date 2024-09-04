package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Account;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface AccountRepository extends R2dbcRepository<Account, Long> {
}
