package org.ethanhao.triprover.dto.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserUpdateDTO extends UserBaseDTO {
    private String password;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String avatar;
} 