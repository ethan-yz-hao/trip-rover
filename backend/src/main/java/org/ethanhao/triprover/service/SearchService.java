package org.ethanhao.triprover.service;

import java.util.List;

import org.ethanhao.triprover.dto.plan.PlanIndexResponseDTO;
import org.ethanhao.triprover.dto.user.UserIndexResponseDTO;

public interface SearchService {
    List<UserIndexResponseDTO> searchUsers(String query);
    List<PlanIndexResponseDTO> searchPlans(String query);
} 