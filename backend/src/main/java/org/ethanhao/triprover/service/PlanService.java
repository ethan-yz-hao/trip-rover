package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanPlace;
import org.ethanhao.triprover.domain.ResponseResult;

import java.util.List;

public interface PlanService {
    Plan getPlan(Long planId);
    List<PlanPlace> getPlanPlace(Long planId);
    boolean isUserAuthorized(Long planId, Long userId);
}
