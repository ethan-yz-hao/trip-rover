package org.ethanhao.triprover.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plan_user_roles")
public class PlanUserRole {

    @EmbeddedId
    private PlanUserRoleId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    public enum RoleType {
        OWNER,
        EDITOR,
        VIEWER
    }
}