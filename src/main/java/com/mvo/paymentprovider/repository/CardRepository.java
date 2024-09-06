package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.Card;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface CardRepository extends R2dbcRepository<Card, UUID> {
}
