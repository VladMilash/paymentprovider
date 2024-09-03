package com.mvo.paymentprovider;

import org.springframework.boot.SpringApplication;

public class TestPaymentProviderApplication {

    public static void main(String[] args) {
        SpringApplication.from(PaymentProviderApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
