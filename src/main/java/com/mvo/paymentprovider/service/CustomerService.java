package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.entity.Customer;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<Customer> findByFirstnameAndLastnameAndCountry(String firstName, String lastName, String country);

    Mono<Customer> createCustomer(Customer customer);
}
