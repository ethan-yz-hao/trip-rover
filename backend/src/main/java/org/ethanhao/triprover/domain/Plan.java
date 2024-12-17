package org.ethanhao.triprover.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
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

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "id.plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlanMember> planMembers = new HashSet<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<PlanPlace> places;

    @CreationTimestamp
    @Column(columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;
}