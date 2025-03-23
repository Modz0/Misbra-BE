package com.Misbra.Mapper;


import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface PhotoMapper {
    PhotoMapper INSTANCE = Mappers.getMapper(PhotoMapper.class);

    PhotoDTO toDTO(Photo entity);

    Photo toEntity(PhotoDTO dto);
}

