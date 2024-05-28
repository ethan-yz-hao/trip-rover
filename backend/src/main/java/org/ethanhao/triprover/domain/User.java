package org.ethanhao.triprover.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User Name
    @NotEmpty
    @Column(nullable = false, columnDefinition = "varchar(64)")
    private String userName;

    // Nickname
    @NotEmpty
    @Column(nullable = false, columnDefinition = "varchar(64)")
    private String nickName;

    // User password
    @NotEmpty
    @Column(nullable = false, columnDefinition = "varchar(64)")
    private String password;

    // User type (0 normal user 1 system user)
    @NotEmpty
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer type;

    // User status (0 normal 1 disabled)
    @NotEmpty
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer status;

    // Email
    @Column(columnDefinition = "varchar(64) default NULL")
    private String email;

    // Phone number
    @Column(columnDefinition = "varchar(32) default NULL")
    private String phoneNumber;

    // Avatar
    @Column(columnDefinition = "varchar(128) default NULL")
    private String avatar;

    // User ID of the creator
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

    // Delete flag (0 exists 1 deleted)
    @Column(columnDefinition = "int default 0")
    private Integer delFlag;

    @ManyToMany
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
