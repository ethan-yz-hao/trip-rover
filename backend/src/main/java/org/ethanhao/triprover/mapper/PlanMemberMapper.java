package org.ethanhao.triprover.mapper;

import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.ethanhao.triprover.dto.plan.member.PlanMemberBaseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PlanMapper.class, UserMapper.class})
public interface PlanMemberMapper {
    
    @Mapping(target = "id.plan.planId", source = "planId")
    @Mapping(target = "id.plan.version", constant = "0L")
    @Mapping(target = "id.user.id", source = "userId")
    PlanMember planMemberDTOToPlanMember(PlanMemberUpdateDTO dto);

    @Mapping(target = "plan.planId", source = "planId")
    @Mapping(target = "plan.version", constant = "0L")
    @Mapping(target = "user.id", source = "userId")
    PlanMemberId planMemberIdDTOToPlanMemberId(PlanMemberBaseDTO dto);
} 