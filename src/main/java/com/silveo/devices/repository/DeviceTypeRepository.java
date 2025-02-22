package com.silveo.devices.repository;

import com.silveo.devices.entity.DeviceType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {
    Optional<DeviceType> findByName(String name);

    // Кастомное каскадное удаление
    @Transactional
    default void deleteByIdCascade(Long id) {
        // 1. Удаляем все связанные DeviceInstance и их параметры
        deleteRelatedDeviceInstances(id);

        // 2. Удаляем сам DeviceType (его parameters удалятся каскадно)
        deleteById(id);
    }

    @Modifying
    @Query("DELETE FROM DeviceInstance di WHERE di.deviceType.id = :deviceTypeId")
    void deleteRelatedDeviceInstances(@Param("deviceTypeId") Long deviceTypeId);
}