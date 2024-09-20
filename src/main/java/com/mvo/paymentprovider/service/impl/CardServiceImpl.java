package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.Card;
import com.mvo.paymentprovider.repository.CardRepository;
import com.mvo.paymentprovider.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    public Mono<Card> createCard(Card card) {
        return cardRepository.save(card)
                .doOnSuccess(savedCard -> log.info("card with id {} has been saved successfully", card.getId()))
                .doOnError(error -> log.error("Failed to saving card", error));
    }

    @Override
    public Mono<Card> findByCardNumber(Long cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .doOnSuccess(card -> log.info("Card with cardNumber {} has been finding successfully", cardNumber))
                .doOnError(error -> log.error("Failed to find account with cardNumber {}", cardNumber))
                .switchIfEmpty(Mono.empty());
    }
}

