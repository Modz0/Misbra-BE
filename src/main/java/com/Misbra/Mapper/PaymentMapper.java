

package com.Misbra.Mapper;


import com.Misbra.DTO.PaymentDTO;
import com.Misbra.Entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentDTO toDTO(Payment entity);

    Payment toEntity(PaymentDTO dto);
}

