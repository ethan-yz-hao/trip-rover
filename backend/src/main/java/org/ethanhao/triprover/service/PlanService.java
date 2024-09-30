package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanUpdateMessage;

public interface PlanService {
    Plan getPlan(Long planId);

    boolean isUserAuthorized(Long planId, Long userId);

    void applyUpdate(Long planId, PlanUpdateMessage updateMessage);
}
