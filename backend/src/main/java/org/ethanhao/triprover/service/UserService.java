package org.ethanhao.triprover.service;

import org.ethanhao.triprover.dto.PlanDTO;
import java.util.List;

public interface UserService {
    List<PlanDTO> getUserPlans(Long userId);
} 