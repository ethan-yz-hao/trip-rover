package org.ethanhao.triprover.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plan_places")
public class PlanPlace {

    @JsonIgnore
    @EmbeddedId
    private PlanPlaceId id;

    @JsonIgnore
    @ManyToOne
    @MapsId("planId")
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private String placeId;

    // Additional methods to access sequenceNumber directly
    @JsonIgnore
    public Integer getSequenceNumber() {
        return id.getSequenceNumber();
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        id.setSequenceNumber(sequenceNumber);
    }
}