package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.PlanUserRole;
import org.ethanhao.triprover.domain.PlanUserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanUserRoleRepository extends JpaRepository<PlanUserRole, PlanUserRoleId> {
    PlanUserRole findByIdPlanPlanIdAndIdUserId(Long planId, Long userId);
}
