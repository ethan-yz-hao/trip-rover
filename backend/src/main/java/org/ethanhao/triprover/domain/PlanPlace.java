package org.ethanhao.triprover.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Column(nullable = false)
    private String placeId;

    @JsonIgnore
    @NotNull
    @Column(nullable = false)
    private Integer sequenceNumber;

    @NotNull
    @Column(nullable = false)
    private String googlePlaceId;

    @NotNull
    @Column(nullable = false)
    private Long staySeconds;
}