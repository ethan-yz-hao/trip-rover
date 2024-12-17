package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.domain.PlanUserRole;
import org.ethanhao.triprover.dto.PlanDTO;

public interface PlanService {
    PlanDTO getPlan(Long planId);

    boolean hasRole(Long planId, Long userId, PlanUserRole.RoleType requiredRole);

    Long applyUpdate(Long planId, PlanUpdateMessage updateMessage);
}
