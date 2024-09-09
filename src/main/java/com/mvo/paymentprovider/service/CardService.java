package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.entity.Card;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CardService {
    Mono<Card> findByCardNumber(Long cardNumber);

    Mono<Card> createCard(Card card);
}
