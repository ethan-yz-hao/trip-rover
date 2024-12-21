package org.ethanhao.triprover.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAuthDTO extends UserBaseDTO {
    @NotBlank(message = "Password is required")
    private String password;
} 