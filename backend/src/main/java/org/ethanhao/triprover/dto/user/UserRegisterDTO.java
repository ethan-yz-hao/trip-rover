package org.ethanhao.triprover.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegisterDTO extends UserAuthDTO {
    @NotBlank(message = "Nickname is required")
    private String nickName;
} 