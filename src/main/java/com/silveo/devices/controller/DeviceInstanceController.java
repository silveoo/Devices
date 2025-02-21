package com.silveo.devices.controller;



import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.entity.dto.DeviceInstanceRequest;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.repository.DeviceTypeRepository;
import com.silveo.devices.service.DeviceInstanceService;
import com.silveo.devices.service.DeviceValidationService;
import com.silveo.devices.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("api/v1/device-instances")
@RequiredArgsConstructor
public class DeviceInstanceController {
    private final DeviceInstanceRepository repository;
    private final DeviceInstanceService deviceInstanceService;
    private final PdfReportService pdfReportService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DeviceInstanceRequest request) {
        log.info("Create device instance. RequestId: {}", request.getRequestId());
        return deviceInstanceService.createInstance(request);
    }

    @GetMapping
    public ResponseEntity<List<DeviceInstance>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/report")
    public ResponseEntity<DeviceInstanceReportDto> generateReport(
            @RequestParam Long instanceId,
            @RequestParam String deviceName) {
        log.info("Generating report for instanceId: {} and deviceName: {}", instanceId, deviceName);
        DeviceInstanceReportDto report = deviceInstanceService.generateReport(instanceId, deviceName);
        return ResponseEntity.ok(report);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam Long instanceId,
            @RequestParam String deviceName) throws IOException{

        DeviceInstanceReportDto report = deviceInstanceService.generateReport(instanceId, deviceName);
        byte[] pdfBytes = pdfReportService.generatePdfReport(report);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report.pdf")
                .body(pdfBytes);
    }
}
