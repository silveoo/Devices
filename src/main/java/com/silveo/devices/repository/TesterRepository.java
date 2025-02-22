package com.silveo.devices.repository;

import com.silveo.devices.entity.Tester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TesterRepository extends JpaRepository<Tester, Long> {
    @Query("SELECT DISTINCT t FROM Tester t " +
            "LEFT JOIN FETCH t.user u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.authorities")
    List<Tester> findAllWithUserRolesAndAuthorities();
}
