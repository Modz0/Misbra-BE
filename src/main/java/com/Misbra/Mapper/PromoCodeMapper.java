package com.Misbra.Mapper;



import com.Misbra.DTO.PromoCodeDTO;
import com.Misbra.Entity.PromoCode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface PromoCodeMapper {
    PromoCodeMapper INSTANCE = Mappers.getMapper(PromoCodeMapper.class);

    PromoCodeDTO toDTO(PromoCode entity);

    PromoCode toEntity(PromoCodeDTO dto);
}

