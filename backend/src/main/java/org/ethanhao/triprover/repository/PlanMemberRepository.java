package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanMemberRepository extends JpaRepository<PlanMember, PlanMemberId> {
    PlanMember findByIdPlanPlanIdAndIdUserId(Long planId, Long userId);
}
