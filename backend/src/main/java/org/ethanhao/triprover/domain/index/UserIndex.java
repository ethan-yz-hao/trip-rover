package org.ethanhao.triprover.domain.index;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "sys_user")
public class UserIndex {
    @Field(type = FieldType.Long)
    private Long id;

    @Field(name = "user_name", type = FieldType.Text)
    @JsonProperty("user_name")
    private String userName;

    @Field(name = "nick_name", type = FieldType.Text)
    @JsonProperty("nick_name")
    private String nickName;

    @Field(type = FieldType.Integer)
    private Integer type;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(name = "del_flag", type = FieldType.Integer)
    @JsonProperty("del_flag")
    private Integer delFlag;
} 