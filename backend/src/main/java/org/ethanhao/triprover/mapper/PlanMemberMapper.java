package org.ethanhao.triprover.mapper;

import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.ethanhao.triprover.dto.plan.member.PlanMemberBaseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberUpdateDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberResponseDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { PlanMapper.class, UserMapper.class })
public interface PlanMemberMapper {

    @Mapping(target = "id.plan.planId", source = "planId")
    @Mapping(target = "id.plan.version", constant = "0L")
    @Mapping(target = "id.user.id", source = "dto.id")
    PlanMember planMemberDTOToPlanMember(PlanMemberUpdateDTO dto, Long planId);

    @Mapping(target = "plan.planId", source = "planId")
    @Mapping(target = "plan.version", constant = "0L")
    @Mapping(target = "user.id", source = "dto.id")
    PlanMemberId planMemberIdDTOToPlanMemberId(PlanMemberBaseDTO dto, Long planId);

    @Mapping(target = "id", source = "id.user.id")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "userName", source = "id.user.userName")
    @Mapping(target = "nickName", source = "id.user.nickName")
    @Mapping(target = "email", source = "id.user.email")
    @Mapping(target = "phoneNumber", source = "id.user.phoneNumber")
    @Mapping(target = "avatar", source = "id.user.avatar")
    PlanMemberResponseDTO planMemberToPlanMemberResponseDTO(PlanMember planMember);
}