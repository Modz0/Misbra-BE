package com.Misbra.Mapper;

import com.Misbra.DTO.QuestionDTO;
import com.Misbra.Entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target = "questionThumbnailUrl", ignore = true) // Ignore automatic mapping
    @Mapping(target = "answerThumbnailUrl", ignore = true) // Ignore automatic mapping
    QuestionDTO toDTO(Question entity);
    Question toEntity(QuestionDTO dto);
}