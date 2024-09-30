package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    boolean existsByPlanIdAndUsers_Id(Long planId, Long userId);
}
