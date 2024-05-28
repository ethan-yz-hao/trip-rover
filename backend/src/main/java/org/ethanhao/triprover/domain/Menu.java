package org.ethanhao.triprover.domain;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "sys_menu")
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Menu name
    @Column(nullable = false)
    private String menuName;

    // Parent menu ID
    @Column(columnDefinition = "bigint default 0")
    private Long parentId;

    // Display order
    @Column(columnDefinition = "int default 0")
    private Integer orderNum;

    // Routing address
    @Column(columnDefinition = "varchar(200) default ''")
    private String path;

    // Component path
    @Column(columnDefinition = "varchar(200) default NULL")
    private String component;

    // Whether it is an external link (0 is, 1 is not)
    @Column(columnDefinition = "int default 1")
    private Integer isFrame;

    // Menu type (M directory C menu F button)
    @Column(columnDefinition = "varchar(1) default ''")
    private String menuType;

    // Menu display status (0 display 1 hidden)
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer visible;

    // Menu status (0 normal 1 disabled)
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer status;

    // Permissions identification
    @Column(columnDefinition = "varchar(100) default NULL")
    private String perms;

    // Menu icon
    @Column(columnDefinition = "varchar(100) default '#'")
    private String icon;

    // Create by
    @Column(columnDefinition = "bigint default NULL")
    private Long createBy;

    // Creation time
    @CreationTimestamp
    @Column(columnDefinition = "timestamp  default CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    // Update by
    @Column(columnDefinition = "bigint default NULL")
    private Long updateBy;

    // Update time
    @UpdateTimestamp
    @Column(columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    // Remarks
    @Column(columnDefinition = "varchar(255) default NULL")
    private String remark;

    // Delete flag (0 exists 1 deleted)
    @Column(columnDefinition = "int default 0")
    private Integer delFlag;

    @ManyToMany(mappedBy = "menus")
    private Set<Role> roles = new HashSet<>();
}
