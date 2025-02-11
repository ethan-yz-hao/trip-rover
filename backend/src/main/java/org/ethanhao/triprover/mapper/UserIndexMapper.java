package org.ethanhao.triprover.mapper;

import org.ethanhao.triprover.domain.index.UserIndex;
import org.ethanhao.triprover.dto.user.UserIndexResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserIndexMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "user_name", target = "userName")
    @Mapping(source = "nick_name", target = "nickName")
    UserIndexResponseDTO toResponseDTO(UserIndex userIndex);
} 