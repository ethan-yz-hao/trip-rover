package org.ethanhao.triprover.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "sys_role")
public class Role implements Serializable {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Role name
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private String roleName;

    // Role permissions string
    @Column(nullable = false, columnDefinition = "varchar(100)")
    private String roleKey;

    // Display order
    @Column(nullable = false, columnDefinition = "int")
    private Integer roleSort;

    // Role status (0 normal 1 disabled)
    @Column(nullable = false, columnDefinition = "int")
    private Integer status;

    // Delete flag (0 exists 1 deleted)
    @Column(columnDefinition = "int default 0")
    private Integer delFlag;

    // Create by
    @Column(columnDefinition = "bigint default NULL")
    private Long createBy;

    // Creation time
    @CreationTimestamp
    @Column(columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    // Update by
    @Column(columnDefinition = "bigint default NULL")
    private Long updateBy;

    // Update time
    @UpdateTimestamp
    @Column(columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    // Remarks
    @Column(columnDefinition = "varchar(500) default NULL")
    private String remark;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "sys_role_menu",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private Set<Menu> menus = new HashSet<>();
}
