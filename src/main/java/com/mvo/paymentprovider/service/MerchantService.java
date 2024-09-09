package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.entity.Merchant;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MerchantService {
    Mono<Merchant> findById(UUID merchantID);
}
