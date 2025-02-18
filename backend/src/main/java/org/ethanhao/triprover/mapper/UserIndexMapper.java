package org.ethanhao.triprover.mapper;

import org.ethanhao.triprover.domain.index.UserIndex;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserIndexMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "nickName", target = "nickName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "avatar", target = "avatar")
    UserResponseDTO toResponseDto(UserIndex userIndex);
}