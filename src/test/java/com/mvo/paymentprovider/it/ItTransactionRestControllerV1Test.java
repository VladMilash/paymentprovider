package com.mvo.paymentprovider.it;

import com.mvo.paymentprovider.config.PostgreTestcontainerConfig;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgreTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItTransactionRestControllerV1Test {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testCustomerCreation() {
        Customer testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .firstname("John")
                .lastname("Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .country("USA")
                .build();

        StepVerifier.create(customerRepository.save(testCustomer))
                .expectNextMatches(savedCustomer ->
                        savedCustomer.getFirstname().equals("John") &&
                                savedCustomer.getLastname().equals("Doe") &&
                                savedCustomer.getCountry().equals("USA") &&
                                savedCustomer.getStatus() == Status.ACTIVE
                )
                .verifyComplete();
    }
}