package org.ethanhao.triprover.plan;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Plan {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @Column(updatable = false)
    private String userId;

    @Column
    private String title;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<PlaceArrayItem> places;

    @Column
    private LocalDateTime startTime;

    @Column @CreationTimestamp
    private LocalDateTime createAt;

}