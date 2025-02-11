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
@Document(indexName = "plans")
public class PlanIndex {
    @Field(type = FieldType.Long)
    @JsonProperty("plan_id")
    private Long planId;

    @Field(name = "plan_name", type = FieldType.Text)
    @JsonProperty("plan_name")
    private String planName;

    @Field(type = FieldType.Text)
    private String description;

    @Field(name = "is_public", type = FieldType.Boolean)
    @JsonProperty("is_public")
    private Boolean isPublic;
} 