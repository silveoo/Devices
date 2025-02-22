package com.silveo.devices.controller;

import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.repository.DeviceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/device-types")
@RequiredArgsConstructor
@Slf4j
public class DeviceTypeController {
    private final DeviceTypeRepository repository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('devicetype:write')")
    public ResponseEntity<DeviceType> create(@RequestBody DeviceType deviceType) {
        log.info("Creating device type {}", deviceType);
        return ResponseEntity.ok(repository.save(deviceType));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('device:read')")
    public ResponseEntity<List<DeviceType>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('device:read')")
    public ResponseEntity<DeviceType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(repository.findById(id).orElse(null));
    }
}
