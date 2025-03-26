package com.Misbra.Mapper;


import com.Misbra.DTO.CategoryDTO;
import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Entity.Category;
import com.Misbra.Entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);


    @Mapping(target = "thumbnailUrl", ignore = true) // Ignore automatic mapping
    CategoryDTO toDTO(Category entity);

    Category toEntity(CategoryDTO dto);
}

