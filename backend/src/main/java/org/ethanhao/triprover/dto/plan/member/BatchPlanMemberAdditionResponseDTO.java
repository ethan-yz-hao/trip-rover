package org.ethanhao.triprover.dto.plan.member;

import java.util.List;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class BatchPlanMemberAdditionResponseDTO {
    private List<PlanMemberUpdateDTO> successful;
    private List<PlanMemberAdditionFailureDTO> failed;
}