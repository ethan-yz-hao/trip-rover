package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.dto.plan.PlanSummaryResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    @Query("SELECT p.planId as planId, p.planName as planName, " +
           "p.createTime as createTime, p.updateTime as updateTime, " +
           "pur.role as role " +
           "FROM Plan p " +
           "JOIN p.planMembers pur " +
           "WHERE pur.id.user.id = :userId")
    List<PlanSummaryResponseDTO> findPlanSummaryByUserId(@Param("userId") Long userId);

    @Query("SELECT p.planId as planId, p.planName as planName, " +
           "p.createTime as createTime, p.updateTime as updateTime, " +
           "pur.role as role " +
           "FROM Plan p " +
           "JOIN p.planMembers pur " +
           "WHERE pur.id.user.id = :userId AND p.planId = :planId")
    PlanSummaryResponseDTO findPlanSummaryByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Long planId);
}
