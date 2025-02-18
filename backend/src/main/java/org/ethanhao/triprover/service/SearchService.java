package org.ethanhao.triprover.service;

import java.util.List;

import org.ethanhao.triprover.dto.plan.PlanIndexResponseDTO;
import org.ethanhao.triprover.dto.user.UserResponseDTO;

public interface SearchService {
    List<UserResponseDTO> searchUsers(String query);

    List<PlanIndexResponseDTO> searchPlans(String query);
}