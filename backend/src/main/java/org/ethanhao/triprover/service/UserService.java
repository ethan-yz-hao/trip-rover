package org.ethanhao.triprover.service;

import org.ethanhao.triprover.dto.GetPlan;
import java.util.List;

public interface UserService {
    List<GetPlan> getUserPlans(Long userId);
} 