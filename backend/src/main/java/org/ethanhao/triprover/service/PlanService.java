package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.dto.PlanPlaces;

public interface PlanService {
    PlanPlaces getPlanPlaces(Long planId);

    boolean hasRole(Long planId, Long userId, PlanMember.RoleType requiredRole);

    Long applyUpdate(Long planId, PlanUpdateMessage updateMessage);
}
