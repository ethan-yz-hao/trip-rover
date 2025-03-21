package org.ethanhao.triprover.mapper;

import org.ethanhao.triprover.domain.index.PlanIndex;
import org.ethanhao.triprover.dto.plan.PlanIndexResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlanIndexMapper {
    @Mapping(source = "planId", target = "planId")
    @Mapping(source = "planName", target = "planName")
    PlanIndexResponseDTO toResponseDTO(PlanIndex planIndex);
} 