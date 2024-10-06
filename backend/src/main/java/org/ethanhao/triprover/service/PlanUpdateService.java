package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.PlanUpdateMessage;

public interface PlanUpdateService {
    Long updatePlanWithMessage(Long planId, PlanUpdateMessage updateMessage);
}
