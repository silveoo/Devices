package com.silveo.devices.repository;

import com.silveo.devices.entity.Role;
import com.silveo.devices.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleName name);

    @Query("SELECT r FROM Role r JOIN FETCH r.authorities")
    List<Role> findAllWithAuthorities();
}
