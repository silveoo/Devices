package com.silveo.devices.repository;

import com.silveo.devices.entity.DeviceInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceInstanceRepository extends JpaRepository<DeviceInstance, Long> {
    List<DeviceInstance> findByDeviceTypeId(Long deviceTypeId);
}