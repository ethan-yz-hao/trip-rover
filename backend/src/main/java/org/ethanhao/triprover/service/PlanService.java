package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.Plan;

public interface PlanService {
    Plan getPlan(Long planId);

    boolean isUserAuthorized(Long planId, Long userId);
}
