package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.CardDTO;
import com.mvo.paymentprovider.entity.Card;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardDTO map(Card card);

    @InheritInverseConfiguration
    Card map(CardDTO cardDTO);
}
