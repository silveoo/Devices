package com.silveo.devices.controller;

import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.repository.DeviceTypeRepository;
import com.silveo.devices.service.DeviceInstanceService;
import com.silveo.devices.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/report")
@Slf4j
@RequiredArgsConstructor
public class ReportController {
    private final DeviceInstanceRepository repository;
    private final DeviceInstanceService deviceInstanceService;
    private final PdfReportService pdfReportService;
    private final DeviceTypeRepository deviceTypeRepository;

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

    @GetMapping(value = "/device-type/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('report:generate')")
    public ResponseEntity<byte[]> generateDeviceTypePdfReport(
            @RequestParam String deviceTypeName) throws IOException {

        DeviceType deviceType = deviceTypeRepository.findByName(deviceTypeName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип устройства не найден"));

        byte[] pdfBytes = pdfReportService.generateDeviceTypePdfReport(deviceType);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=device-type-report.pdf")
                .body(pdfBytes);
    }

    @GetMapping(value = "/all-device-types/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('report:generate')")
    public ResponseEntity<byte[]> generateAllDeviceTypesReport() throws IOException {
        List<DeviceType> allDeviceTypes = deviceTypeRepository  .findAll(); // Предполагаем наличие метода findAll()

        if (allDeviceTypes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Нет устройств в базе");
        }

        byte[] pdfBytes = pdfReportService.generateAllDeviceTypesReport(allDeviceTypes);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=all-devices-report.pdf")
                .body(pdfBytes);
    }
}
