package org.ethanhao.triprover.repository;

import org.ethanhao.triprover.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleKey(String roleName);
}
