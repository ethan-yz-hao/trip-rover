package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.domain.PlanUserRole;

public interface PlanService {
    Plan getPlan(Long planId);

    boolean hasRole(Long planId, Long userId, PlanUserRole.RoleType requiredRole);

    Long applyUpdate(Long planId, PlanUpdateMessage updateMessage);
}
