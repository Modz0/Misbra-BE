package com.Misbra.Mapper;


import com.Misbra.DTO.CategoryDTO;
import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Entity.Category;
import com.Misbra.Entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDTO toDTO(Category entity);

    Category toEntity(CategoryDTO dto);
}

