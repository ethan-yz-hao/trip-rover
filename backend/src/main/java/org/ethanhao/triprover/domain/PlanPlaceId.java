package org.ethanhao.triprover.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlanPlaceId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long planId;

    private Integer sequenceNumber;
}