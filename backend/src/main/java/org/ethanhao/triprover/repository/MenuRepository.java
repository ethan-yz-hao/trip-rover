package org.ethanhao.triprover.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.ethanhao.triprover.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>{

//    @Query("""
//        SELECT DISTINCT
//                    m.perms
//                FROM
//                    sys_user_role ur
//                        LEFT JOIN sys_role r ON ur.role_id = r.id
//                        LEFT JOIN sys_role_menu rm ON ur.role_id = rm.role_id
//                        LEFT JOIN sys_menu m  ON m.id = rm.menu_id
//                WHERE
//                    user_id = :userId
//                    AND r.status = 0
//                    AND m.status = 0
//        """)

    @Query("SELECT DISTINCT m.perms FROM Menu m " +
            "JOIN m.roles r " +
            "JOIN r.users u " +
            "WHERE u.id = :userId AND r.status = '0' AND m.status = '0'")
    List<String> findPermsByUserId(@Param("userId") Long userId);
}
