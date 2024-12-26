package org.ethanhao.triprover.mapper;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.dto.plan.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "planMembers", ignore = true)
    @Mapping(target = "places", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Plan planBaseDtoToPlan(PlanBaseDTO dto);

    PlanPlacesResponseDTO planToPlanPlacesResponseDto(Plan plan);

    @Mapping(target = "planId", source = "id")
    @Mapping(target = "planName", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "planMembers", ignore = true)
    @Mapping(target = "places", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Plan idToPlan(Long id);

    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "planMembers", ignore = true)
    @Mapping(target = "places", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "planName", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "description", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isPublic", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePlanFromDto(PlanUpdateDTO dto, @MappingTarget Plan plan);
}