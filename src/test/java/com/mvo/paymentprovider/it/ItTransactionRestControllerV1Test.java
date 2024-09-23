package com.mvo.paymentprovider.it;

import com.mvo.paymentprovider.config.PostgreTestcontainerConfig;
import com.mvo.paymentprovider.dto.RequestDTO;
import com.mvo.paymentprovider.dto.TransactionDTO;
import com.mvo.paymentprovider.entity.*;
import com.mvo.paymentprovider.repository.*;
import com.mvo.paymentprovider.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
    private WebhookRepository webhookRepository;

    private Merchant testMerchant;
    private Customer testCustomer;
    private Account testMerchantAccount;
    private Account testCustomerAccount;
    private Card testCard;
    private Transaction testTransaction;
    private String basicAuthHeader;

    private static final String TEST_PASSWORD = "1";


    @BeforeEach
    public void setup() {
        webhookRepository.deleteAll().block();
        transactionRepository.deleteAll().block();
        cardRepository.deleteAll().block();
        accountRepository.deleteAll().block();
        merchantRepository.deleteAll().block();
        transactionRepository.deleteAll().block();
        customerRepository.deleteAll().block();

        testMerchant = DataUtils.getTransientMerchant();
        testMerchant = merchantRepository.save(testMerchant).block();

        testCustomer = DataUtils.getTransientCustomer();
        testCustomer = customerRepository.save(testCustomer).block();

        testMerchantAccount = DataUtils.getTransientAccountWithoutBalanceAndOwnerTypeAndOwnerId();
        testMerchantAccount.setMerchantId(testMerchant.getId());
        testMerchantAccount.setBalance(BigDecimal.valueOf(0));
        testMerchantAccount.setOwnerType("merchant");
        testMerchantAccount = accountRepository.save(testMerchantAccount).block();

        testCustomerAccount = DataUtils.getTransientAccountWithoutBalanceAndOwnerTypeAndOwnerId();
        testCustomerAccount.setCustomerId(testCustomer.getId());
        testCustomerAccount.setBalance(BigDecimal.valueOf(1000));
        testCustomerAccount.setOwnerType("customer");
        testCustomerAccount = accountRepository.save(testCustomerAccount).block();

        testCard = DataUtils.getTransientCard();
        testCard = cardRepository.save(testCard).block();

        String auth = testMerchant.getId() + ":" + TEST_PASSWORD;
        basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));


    }

    @Test
    public void testTopUpEndpoint() {
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

        Account updatedMerchantAccount = accountRepository.findById(testMerchantAccount.getId()).block();
        Account updatedCustomerAccount = accountRepository.findById(testCustomerAccount.getId()).block();

        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(updatedMerchantAccount.getBalance()),
                "Merchant account balance should be 100.00");
        assertEquals(0, BigDecimal.valueOf(900.00).compareTo(updatedCustomerAccount.getBalance()),
                "Customer account balance should be 900.00");

    }

    @Test
    public void testTopUpEndpointUnauthorized() {
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
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    public void testGetTransactions() {
        LocalDate specificDate = LocalDate.of(2023, 9, 15);
        LocalDateTime startOfDay = specificDate.atStartOfDay(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endOfDay = specificDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toLocalDateTime();

        testTransaction = Transaction.builder()
                .transactionStatus(TransactionStatus.SUCCESS)
                .message("Test Transaction")
                .createdAt(startOfDay.plusHours(1))
                .updatedAt(startOfDay.plusHours(1))
                .paymentMethod("CARD")
                .amount(new BigDecimal(100))
                .operationType(OperationType.TOP_UP)
                .notificationUrl("test")
                .merchantAccountId(testMerchantAccount.getId())
                .currency("USD")
                .language("Eng")
                .build();
        testTransaction = transactionRepository.save(testTransaction).block();
        System.out.println("Test transaction created at: " + testTransaction.getCreatedAt());

        assertNotNull(testTransaction, "Test transaction should not be null");


        List<Transaction> allTransactions = transactionRepository.findAll().collectList().block();
        if (allTransactions != null) {
            allTransactions.forEach(transaction ->
                    System.out.println("Transaction ID: " + transaction.getId() +
                            ", Created At: " + transaction.getCreatedAt() +
                            ", Status: " + transaction.getTransactionStatus()));
        } else {
            System.out.println("No transactions found in the database.");
        }

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/transactions/")
                        .queryParam("start_date", startOfDay.toEpochSecond(ZoneOffset.UTC))
                        .queryParam("end_date", endOfDay.toEpochSecond(ZoneOffset.UTC))
                        .build())
                .header("Authorization", basicAuthHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDTO.class)
                .value(transactions -> {
                    assertEquals(1, transactions.size(), "Transactions list should contain 1 transaction");
                    TransactionDTO dto = transactions.getFirst();
                    assertNotNull(dto.getId(), "Transaction ID should not be null");
                    assertEquals(TransactionStatus.SUCCESS, dto.getTransactionStatus(), "Transaction status should be SUCCESS");
                });
    }

    @Test
    public void testGetTransactionDetails() {
        Transaction testTransaction = Transaction.builder()
                .transactionStatus(TransactionStatus.SUCCESS)
                .message("Ok")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .paymentMethod("CARD")
                .amount(new BigDecimal(100))
                .operationType(OperationType.TOP_UP)
                .notificationUrl("test")
                .merchantAccountId(testMerchantAccount.getId())
                .currency("USD")
                .language("Eng")
                .build();
        transactionRepository.save(testTransaction).block();

        webTestClient.get()
                .uri("/api/v1/transactions/" + testTransaction.getId().toString())
                .header("Authorization", basicAuthHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .value(responseBody -> {
                    System.out.println("Response Body: " + responseBody);
                    assertNotNull(responseBody.getId());
                    assertNotNull(responseBody.getTransactionStatus());
                    assertEquals("Ok", responseBody.getMessage());
                });
    }


//    This is an additional test that I did to check if the repository is working correctly.
//    This was necessary in order to narrow down the search for the cause of the error
    @Test
    public void testTransactionRepositoryGetTransactions() {
        LocalDate specificDate = LocalDate.of(2023, 9, 15);
        LocalDateTime startOfDay = specificDate.atStartOfDay(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endOfDay = specificDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toLocalDateTime();

        Transaction testTransaction = Transaction.builder()
                .transactionStatus(TransactionStatus.SUCCESS)
                .message("Test Transaction")
                .createdAt(startOfDay.plusHours(1))
                .updatedAt(startOfDay.plusHours(1))
                .paymentMethod("CARD")
                .amount(new BigDecimal(100))
                .operationType(OperationType.TOP_UP)
                .notificationUrl("test")
                .merchantAccountId(testMerchantAccount.getId())
                .currency("USD")
                .language("Eng")
                .build();
        transactionRepository.save(testTransaction).block();

        Flux<Transaction> transactions = transactionRepository.getTransactionsByCreatedAtBetweenAndOperationTypeAndMerchantAccountId(
                startOfDay,
                endOfDay,
                OperationType.TOP_UP,
                testMerchantAccount.getId()
        );

        transactions.doOnNext(transaction ->
                System.out.println("Transaction ID: " + transaction.getId() +
                        ", Created At: " + transaction.getCreatedAt() +
                        ", Status: " + transaction.getTransactionStatus())
        ).blockLast();

        List<Transaction> transactionList = transactions.collectList().block();
        assertNotNull(transactionList, "Transaction list should not be null");
        assertEquals(1, transactionList.size(), "Transaction list should contain 1 transaction");

        Transaction foundTransaction = transactionList.getFirst();
        assertNotNull(foundTransaction.getId(), "Transaction ID should not be null");
        assertEquals(TransactionStatus.SUCCESS, foundTransaction.getTransactionStatus(), "Transaction status should be SUCCESS");
    }
}