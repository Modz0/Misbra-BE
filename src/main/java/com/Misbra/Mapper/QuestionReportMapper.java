

package com.Misbra.Mapper;


import com.Misbra.DTO.QuestionReportDTO;
import com.Misbra.Entity.QuestionReport;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface QuestionReportMapper {
    QuestionReportMapper INSTANCE = Mappers.getMapper(QuestionReportMapper.class);

    QuestionReportDTO toDTO(QuestionReport entity);

    QuestionReport toEntity(QuestionReportDTO dto);

}
