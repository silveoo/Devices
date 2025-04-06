package com.silveo.devices.controller;



import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.entity.dto.DeviceInstanceRequestDto;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.service.DeviceInstanceService;
import com.silveo.devices.service.PdfReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/device-instances")
@RequiredArgsConstructor
@Tag(name = "Device Instance", description = "Экземпляры устройств для будущих отчетов")
public class DeviceInstanceController {
    private final DeviceInstanceRepository repository;
    private final DeviceInstanceService deviceInstanceService;
    private final PdfReportService pdfReportService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('device:write')")
    @Operation(summary = "Создание экземпляра", description = "Создание экземпляра устройства и запись параметров его тестирования")
    public ResponseEntity<?> create(@RequestBody DeviceInstanceRequestDto request) throws AccessDeniedException {
        log.info("Create device instance. RequestId: {}", request.getRequestId());
        return deviceInstanceService.createInstance(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('device:read')")
    @Operation(summary = "Получение экземпляров устройств", description = "Получение всех экземпляров всех устройств")
    public ResponseEntity<List<DeviceInstance>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/by-type/")
    @PreAuthorize("hasAnyAuthority('device:read')")
    @Operation(summary = "Получение экземпляров устройств")
    public ResponseEntity<List<DeviceInstance>> getAllByType(
            @RequestParam(required = false) Long deviceTypeId // Параметр для фильтрации
    ) {
        if (deviceTypeId != null) {
            return ResponseEntity.ok(repository.findByDeviceTypeId(deviceTypeId));
        }
        return ResponseEntity.ok(repository.findAll());
    }

}
