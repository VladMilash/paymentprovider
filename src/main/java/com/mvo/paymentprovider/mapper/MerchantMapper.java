package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.MerchantDTO;
import com.mvo.paymentprovider.entity.Merchant;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MerchantMapper {
    MerchantDTO map(Merchant merchant);

    @InheritInverseConfiguration
    Merchant map(MerchantDTO merchantDTO);
}
