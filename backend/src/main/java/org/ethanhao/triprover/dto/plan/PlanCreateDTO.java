package org.ethanhao.triprover.dto.plan;

import java.util.List;

import org.ethanhao.triprover.domain.PlanPlace;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlanCreateDTO extends PlanBaseDTO {
    private List<PlanPlace> places;
}