package com.silveo.devices.controller;

import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.repository.DeviceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/device-types")
@RequiredArgsConstructor
public class DeviceTypeController {
    private final DeviceTypeRepository repository;

    @PostMapping
    public ResponseEntity<DeviceType> create(@RequestBody DeviceType deviceType) {
        return ResponseEntity.ok(repository.save(deviceType));
    }

    @GetMapping
    public ResponseEntity<List<DeviceType>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }
}
