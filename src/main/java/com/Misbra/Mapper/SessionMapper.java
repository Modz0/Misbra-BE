package com.Misbra.Mapper;


import com.Misbra.DTO.CategoryDTO;
import com.Misbra.DTO.PhotoDTO;
import com.Misbra.DTO.SessionDTO;
import com.Misbra.Entity.Category;
import com.Misbra.Entity.Photo;
import com.Misbra.Entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionMapper INSTANCE = Mappers.getMapper(SessionMapper.class);

    SessionDTO toDTO(Session entity);

    Session toEntity(SessionDTO dto);
}

