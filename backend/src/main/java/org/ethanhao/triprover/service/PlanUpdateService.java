package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.PlanUpdateMessage;

public interface PlanUpdateService {
    void updatePlanWithMessage(Long planId, PlanUpdateMessage updateMessage);
}
