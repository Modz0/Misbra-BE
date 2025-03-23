package com.Misbra.Mapper;


import com.Misbra.Authentication.DTO.AuthResponse.UserResponseDTO;
import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO toDTO(User entity);

    User toEntity(UserDTO dto);

    UserResponseDTO toAuthResponseDTO(User entity);
}
