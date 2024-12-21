package org.ethanhao.triprover.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserBaseDTO {
    @NotBlank(message = "Username is required")
    private String userName;
}
