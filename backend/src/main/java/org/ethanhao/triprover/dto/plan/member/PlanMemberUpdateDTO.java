package org.ethanhao.triprover.dto.plan.member;

import org.ethanhao.triprover.domain.PlanMember;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlanMemberUpdateDTO extends PlanMemberBaseDTO {

    @NotNull(message = "Role is required")
    private PlanMember.RoleType role;
}