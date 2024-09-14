package com.mvo.paymentprovider.security;

import com.mvo.paymentprovider.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class MerchantDetailService implements ReactiveUserDetailsService {

    private final MerchantService merchantService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return merchantService.findById(UUID.fromString(username))
                .map(merchant -> (UserDetails) new MerchantDetails(merchant))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Merchant not found with id: " + username)));
    }
}
