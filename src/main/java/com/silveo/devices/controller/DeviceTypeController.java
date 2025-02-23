package com.silveo.devices.controller;

import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.repository.DeviceTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Device Type", description = "Типы устройств, определение их параметров")
public class DeviceTypeController {
    private final DeviceTypeRepository repository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('devicetype:write')")
    @Operation(summary = "Создание типа устройства",
            description = "Создание типа устройства и задание его полей и параметров, например LESS_THAN, RANGE и т.д.")
    public ResponseEntity<DeviceType> create(@RequestBody DeviceType deviceType) {
        log.info("Creating device type {}", deviceType);
        return ResponseEntity.ok(repository.save(deviceType));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('device:read')")
    @Operation(summary = "Получение всех типов устройств")
    public ResponseEntity<List<DeviceType>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('device:read')")
    @Operation(summary = "Получение устройства", description = "Получение одного типа устройства по его id")
    public ResponseEntity<DeviceType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(repository.findById(id).orElse(null));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('devicetype:write')")
    @Operation(summary = "Удаление типа устройства", description = "Каскадное удаление типа устройства и всех его экземпляров")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        log.info("Deleting device type {}", id);
        repository.deleteByIdCascade(id);
        return ResponseEntity.ok().build();
    }
}
