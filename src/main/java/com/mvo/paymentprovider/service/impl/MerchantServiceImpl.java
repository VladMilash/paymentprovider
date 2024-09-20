package com.mvo.paymentprovider.service.impl;

import com.mvo.paymentprovider.entity.Merchant;
import com.mvo.paymentprovider.repository.MerchantRepository;
import com.mvo.paymentprovider.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    @Override
    public Mono<Merchant> findById(UUID merchantID) {
        return merchantRepository.findById(merchantID)
                .doOnSuccess(merchant -> log.info("merchant with id {} has been finding successfully", merchant))
                .doOnError(error -> log.error("Failed to find merchant with id {}", merchantID))
                .switchIfEmpty(Mono.empty());
    }
}
