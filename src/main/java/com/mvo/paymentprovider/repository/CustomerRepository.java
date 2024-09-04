package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Customer;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CustomerRepository extends R2dbcRepository<Customer, Long> {
}
