package org.ethanhao.triprover.dto.user;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String userName;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String avatar;
    private Integer status;
    private Integer type;
}