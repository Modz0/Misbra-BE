
package com.Misbra.Mapper;



import com.Misbra.DTO.SessionBundleDTO;

import com.Misbra.Entity.SessionBundle;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface SessionBundleMapper {
    SessionBundleMapper INSTANCE = Mappers.getMapper(SessionBundleMapper.class);

    SessionBundleDTO toDTO(SessionBundle entity);

    SessionBundle toEntity(SessionBundleDTO dto);
}

