package org.ethanhao.triprover.service;

import org.ethanhao.triprover.dto.PostPlan;
import org.ethanhao.triprover.dto.GetPlan;
import java.util.List;

public interface UserService {
    List<GetPlan> getUserPlans(Long userId);
    GetPlan createPlan(Long userId, PostPlan request);
} 