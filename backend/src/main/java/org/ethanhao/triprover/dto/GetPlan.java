package org.ethanhao.triprover.dto;

import java.time.LocalDateTime;
import org.ethanhao.triprover.domain.PlanUserRole.RoleType;

public interface GetPlan {
    Long getPlanId();
    String getPlanName();
    RoleType getRole();
    LocalDateTime getCreateTime();
    LocalDateTime getUpdateTime();
} 