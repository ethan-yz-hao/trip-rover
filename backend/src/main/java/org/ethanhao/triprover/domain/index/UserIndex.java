package org.ethanhao.triprover.domain.index;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "users")
public class UserIndex {
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String user_name;


    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String nick_name;

    @Field(type = FieldType.Integer)
    private Integer type;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Integer)
    private Integer del_flag;
} 