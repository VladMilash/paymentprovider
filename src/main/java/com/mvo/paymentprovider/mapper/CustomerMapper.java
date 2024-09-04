package com.mvo.paymentprovider.mapper;

import com.mvo.paymentprovider.dto.CustomerDTO;
import com.mvo.paymentprovider.entity.Customer;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDTO map(Customer customer);

    @InheritInverseConfiguration
    Customer map(CustomerDTO customerDTO);
}
