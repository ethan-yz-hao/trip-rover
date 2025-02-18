package org.ethanhao.triprover.dto.plan.member;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlanMemberResponseDTO extends PlanMemberUpdateDTO {

    private String userName;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String avatar;
}