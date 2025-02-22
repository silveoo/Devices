package com.silveo.devices.controller;

import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.service.DeviceInstanceService;
import com.silveo.devices.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/report")
@Slf4j
@RequiredArgsConstructor
public class ReportController {
    private final DeviceInstanceRepository repository;
    private final DeviceInstanceService deviceInstanceService;
    private final PdfReportService pdfReportService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('report:generate')")
    public ResponseEntity<DeviceInstanceReportDto> generateReport(
            @RequestParam Long instanceId,
            @RequestParam String deviceName) {
        log.info("Generating report for instanceId: {} and deviceName: {}", instanceId, deviceName);
        DeviceInstanceReportDto report = deviceInstanceService.generateReport(instanceId, deviceName);
        return ResponseEntity.ok(report);
    }


    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('report:generate')")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam Long instanceId,
            @RequestParam String deviceName) throws IOException {

        log.info("Generating PDF report for instanceId: {} and deviceName: {}", instanceId, deviceName);

        DeviceInstanceReportDto report = deviceInstanceService.generateReport(instanceId, deviceName);
        byte[] pdfBytes = pdfReportService.generatePdfReport(report);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report.pdf")
                .body(pdfBytes);
    }
}
