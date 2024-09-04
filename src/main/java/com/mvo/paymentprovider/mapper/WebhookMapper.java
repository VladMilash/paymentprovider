package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.WebhookDTO;
import com.mvo.paymentprovider.entity.Webhook;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WebhookMapper {
    WebhookDTO map(Webhook webhook);

    @InheritInverseConfiguration
    Webhook map(WebhookDTO webhookDTO);
}
