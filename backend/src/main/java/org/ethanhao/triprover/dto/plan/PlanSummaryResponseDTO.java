package org.ethanhao.triprover.dto.plan;

import java.time.LocalDateTime;
import org.ethanhao.triprover.domain.PlanMember.RoleType;

public interface PlanSummaryResponseDTO {
    Long getPlanId();
    String getPlanName();
    boolean getIsPublic();
    String getDescription();
    RoleType getRole();
    LocalDateTime getCreateTime();
    LocalDateTime getUpdateTime();
} 