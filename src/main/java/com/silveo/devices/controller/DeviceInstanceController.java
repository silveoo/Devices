package com.silveo.devices.controller;



import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.entity.dto.DeviceInstanceRequestDto;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.service.DeviceInstanceService;
import com.silveo.devices.service.PdfReportService;
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
public class DeviceInstanceController {
    private final DeviceInstanceRepository repository;
    private final DeviceInstanceService deviceInstanceService;
    private final PdfReportService pdfReportService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('device:write')")
    public ResponseEntity<?> create(@RequestBody DeviceInstanceRequestDto request) throws AccessDeniedException {
        log.info("Create device instance. RequestId: {}", request.getRequestId());
        return deviceInstanceService.createInstance(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('device:read')")
    public ResponseEntity<List<DeviceInstance>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

}
