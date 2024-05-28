package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);
    User findByUserNameAndDelFlag(String userName, Integer delFlag);
}
