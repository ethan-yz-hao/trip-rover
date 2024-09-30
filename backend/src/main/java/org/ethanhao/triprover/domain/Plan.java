package org.ethanhao.triprover.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    private String planName;

    @Version
    private Long version; // Version field for optimistic locking

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_plans",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<PlanPlace> places;
}