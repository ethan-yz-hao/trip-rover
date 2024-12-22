package org.ethanhao.triprover.dto.plan;

import java.time.LocalDateTime;
import org.ethanhao.triprover.domain.PlanMember.RoleType;

public interface PlanSummaryResponseDTO {
    Long getPlanId();
    String getPlanName();
    RoleType getRole();
    LocalDateTime getCreateTime();
    LocalDateTime getUpdateTime();
} 