package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlanMemberRepository extends JpaRepository<PlanMember, PlanMemberId> {
    @Query("SELECT pm FROM PlanMember pm " +
            "JOIN FETCH pm.id.user " +
            "WHERE pm.id.plan.planId = :planId")
    List<PlanMember> findByPlanIdWithUser(@Param("planId") Long planId);

    @Query("SELECT pm FROM PlanMember pm " +
            "JOIN FETCH pm.id.user " +
            "WHERE pm.id.plan.planId = :planId AND pm.id.user.id = :userId")
    PlanMember findByPlanIdAndUserIdWithUser(@Param("planId") Long planId, @Param("userId") Long userId);
}
