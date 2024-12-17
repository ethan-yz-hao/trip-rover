package org.ethanhao.triprover.service;

import org.ethanhao.triprover.dto.PlanCreation;
import org.ethanhao.triprover.dto.PlanSummary;
import java.util.List;

public interface UserService {
    List<PlanSummary> getUserPlans(Long userId);
    PlanSummary createPlan(Long userId, PlanCreation request);
} 