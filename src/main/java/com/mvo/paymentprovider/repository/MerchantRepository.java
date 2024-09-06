package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Merchant;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface MerchantRepository extends R2dbcRepository<Merchant, UUID> {
}
