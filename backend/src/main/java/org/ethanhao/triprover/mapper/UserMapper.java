package org.ethanhao.triprover.mapper;

import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.user.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "planMembers", ignore = true)
    @Mapping(target = "nickName", ignore = true)
    User userAuthDtoToUser(UserAuthDTO dto);

    @InheritConfiguration
    @Mapping(target = "nickName", source = "nickName")
    User userRegisterDtoToUser(UserRegisterDTO dto);

    UserResponseDTO toResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "planMembers", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateUserFromDto(UserUpdateDTO dto, @MappingTarget User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "planMembers", ignore = true)
    @Mapping(target = "nickName", ignore = true)
    User idToUser(Long id);

    @AfterMapping
    default void setDefaults(@MappingTarget User user) {
        if (user.getStatus() == null) user.setStatus(0);
        if (user.getType() == null) user.setType(0);
        if (user.getDelFlag() == null) user.setDelFlag(0);
    }
} 