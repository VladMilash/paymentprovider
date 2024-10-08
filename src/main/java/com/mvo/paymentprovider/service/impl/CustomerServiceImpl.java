package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.Customer;
import com.mvo.paymentprovider.repository.CustomerRepository;
import com.mvo.paymentprovider.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    public Mono<Customer> findByFirstnameAndLastnameAndCountry(String firstName, String lastName, String country) {
        return customerRepository.findByFirstnameAndLastnameAndCountry(firstName, lastName, country)
                .doOnSuccess(customer -> log.info("Customer with firstname {} and lastname {} and country has been found successfully {}", firstName, lastName, country))
                .doOnError(error -> log.error("Failed to find customer with firstname {} and lastname {} and country {}", firstName, lastName, country))
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Customer> createCustomer(Customer customer) {
        return customerRepository.save(customer)
                .doOnSuccess(savedCustomer -> log.info("customer with id {} has been saved successfully", customer.getId()))
                .doOnError(error -> log.error("Failed to saving customer", error));
    }
}
