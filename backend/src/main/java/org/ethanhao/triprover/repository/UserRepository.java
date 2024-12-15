package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);
    User findByUserNameAndDelFlag(String userName, Integer delFlag);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email AND u.delFlag = :delFlag")
    User findByEmailAndDelFlag(@Param("email") String email, @Param("delFlag") int delFlag);
}
