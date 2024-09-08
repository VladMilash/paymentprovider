package com.mvo.paymentprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class PaymentProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProviderApplication.class, args);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }

}
