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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private String placeId;

    @JsonIgnore
    private Integer sequenceNumber;

    private String googlePlaceId;

    private Long staySeconds;
}