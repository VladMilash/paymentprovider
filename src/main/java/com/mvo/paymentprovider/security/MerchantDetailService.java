package com.mvo.paymentprovider.security;

import com.mvo.paymentprovider.entity.Status;
import com.mvo.paymentprovider.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantDetailService implements ReactiveUserDetailsService {

    private final MerchantService merchantService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return merchantService.findById(UUID.fromString(username))
                .flatMap(merchant -> {
                    if (!merchant.getStatus().equals(Status.ACTIVE)) {
                        return Mono.error(new UsernameNotFoundException("Merchant account is not ACTIVE"));
                    }
                    return Mono.just((UserDetails) new MerchantDetails(merchant));

                })
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Merchant not found with id: " + username)));
    }

}