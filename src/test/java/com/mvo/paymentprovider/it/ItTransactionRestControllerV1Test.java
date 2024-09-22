package com.mvo.paymentprovider.it;

import com.mvo.paymentprovider.config.PostgreTestcontainerConfig;
import com.mvo.paymentprovider.dto.RequestDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.notification.WebhookService;
import com.mvo.paymentprovider.repository.*;
import com.mvo.paymentprovider.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.http.MediaType;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgreTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItTransactionRestControllerV1Test {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private TransactionService transactionService;

    private Merchant testMerchant;
    private Customer testCustomer;
    private Account testMerchantAccount;
    private Account testCustomerAccount;
    private Card testCard;
    private String basicAuthHeader;

    private static final String TEST_PASSWORD = "1";


    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll().block();
        cardRepository.deleteAll().block();
        accountRepository.deleteAll().block();
        merchantRepository.deleteAll().block();
        customerRepository.deleteAll().block();

        testMerchant = Merchant.builder()
                .name("MerchantTest")
                .secretKey(Base64.getEncoder().encodeToString(TEST_PASSWORD.getBytes()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
        testMerchant = merchantRepository.save(testMerchant).block();

        testCustomer = Customer.builder()
                .firstname("John")
                .lastname("Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .country("USA")
                .build();
        testCustomer = customerRepository.save(testCustomer).block();

        testMerchantAccount = Account.builder()
                .merchantId(testMerchant.getId())
                .ownerType("merchant")
                .currency("USD")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
        testMerchantAccount = accountRepository.save(testMerchantAccount).block();

        testCustomerAccount = Account.builder()
                .customerId(testCustomer.getId())
                .ownerType("customer")
                .currency("USD")
                .balance(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
        testCustomerAccount = accountRepository.save(testCustomerAccount).block();

        testCard = Card.builder()
                .accountId(testCustomerAccount.getId())
                .cardNumber(1234567890123456L)
                .expDate("12/25")
                .cvv("123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
        testCard = cardRepository.save(testCard).block();

        String auth = testMerchant.getId() + ":" + TEST_PASSWORD;
        basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));


    }

    @Test
    public void testTopUp() {
        RequestDTO requestDTO = RequestDTO.builder()
                .paymentMethod("CARD")
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .cardNumber(testCard.getCardNumber())
                .expDate(testCard.getExpDate())
                .cvv(testCard.getCvv())
                .language("en")
                .notificationUrl("http://example.com/notify")
                .firstName(testCustomer.getFirstname())
                .lastName(testCustomer.getLastname())
                .country(testCustomer.getCountry())
                .build();

        webTestClient.post()
                .uri("/api/v1/transactions/")
                .header("Authorization", basicAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .value(responseBody -> {
                    assertNotNull(responseBody.getId());
                    assertNotNull(responseBody.getTransactionStatus());
                    assertEquals("OK", responseBody.getMessage());
                });

        // Verify the database state after the transaction
        Account updatedMerchantAccount = accountRepository.findById(testMerchantAccount.getId()).block();
        Account updatedCustomerAccount = accountRepository.findById(testCustomerAccount.getId()).block();

        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(updatedMerchantAccount.getBalance()),
                "Merchant account balance should be 100.00");
        assertEquals(0, BigDecimal.valueOf(900.00).compareTo(updatedCustomerAccount.getBalance()),
                "Customer account balance should be 900.00");

    }


}