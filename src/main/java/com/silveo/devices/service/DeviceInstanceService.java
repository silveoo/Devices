package com.silveo.devices.service;

import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.entity.dto.DeviceInstanceRequest;
import com.silveo.devices.entity.embed.DeviceParameter;
import com.silveo.devices.entity.embed.DeviceParameterTemplate;
import com.silveo.devices.entity.enums.ParameterType;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.repository.DeviceTypeRepository;
import com.silveo.devices.repository.TesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceInstanceService {
    private final DeviceInstanceRepository repository;
    private final DeviceValidationService deviceValidationService;
    private final DeviceTypeRepository typeRepository;
    private final TesterRepository testerRepository;

    public ResponseEntity<?> createInstance(DeviceInstanceRequest request) {
        DeviceType type = typeRepository.findByName(request.getDeviceName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device type not found"));
        Tester tester = testerRepository.findById(request.getTesterId())
                .orElseThrow(() -> new RuntimeException("Тестировщик не найден"));

        DeviceInstance instance = new DeviceInstance();
        instance.setDeviceType(type);
        instance.setParameters(request.getParameters());
        instance.setTester(tester);
        repository.save(instance);

        boolean isValid = deviceValidationService.validate(type, instance);
        return ResponseEntity.ok(Map.of("instance", instance, "valid", isValid));
    }

    public DeviceInstanceReportDto generateReport(Long instanceId, String deviceName) {
        DeviceInstance instance = repository.findById(instanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device instance not found"));
        DeviceType type = typeRepository.findByName(deviceName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device type not found"));
        Tester tester = instance.getTester();

        // Формируем ожидаемые параметры (с учетом типа RANGE)
        Map<String, String> expectedParameters = type.getParameters().stream()
                .filter(Objects::nonNull)
                .filter(template -> template.getName() != null)
                .collect(Collectors.toMap(
                        DeviceParameterTemplate::getName,
                        template -> switch (template.getType()) {
                            case RANGE -> {
                                if (template.getMinValue() == null || template.getMaxValue() == null) {
                                    yield "Invalid range configuration";
                                }
                                yield String.format("[%s - %s]", template.getMinValue(), template.getMaxValue());
                            }
                            case ENUM -> {
                                if (template.getAllowedValues() == null || template.getAllowedValues().isEmpty()) {
                                    yield "No allowed values specified";
                                }
                                yield "Allowed: " + template.getAllowedValues();
                            }
                            case DEVIATION -> String.format(
                                    "%s ±%s%%",
                                    template.getValue(),
                                    template.getTolerancePercent()
                            );
                            case GREATER_THAN -> ">" + template.getValue();
                            case LESS_THAN -> "<" + template.getValue();
                            case EQUALS, EQUALS_STRING -> template.getValue();
                            default -> template.getValue() != null ? template.getValue() : "N/A";
                        }
                ));

        Map<String, String> actualParameters = instance.getParameters().stream()
                .filter(Objects::nonNull)
                .filter(param -> param.getName() != null && param.getValue() != null)
                .collect(Collectors.toMap(
                        DeviceParameter::getName,
                        DeviceParameter::getValue
                ));

        Map<String, String> discrepancies = new HashMap<>();
        for (DeviceParameterTemplate template : type.getParameters()) {
            if (template.getName() == null) continue;

            String expectedValue = expectedParameters.get(template.getName());
            String actualValue = actualParameters.get(template.getName());

            boolean isValid = actualValue != null && deviceValidationService.validateParameter(template, actualValue);

            if (!isValid) {
                discrepancies.put(
                        template.getName(),
                        String.format("Expected: %s | Actual: %s", expectedValue, actualValue != null ? actualValue : "N/A")
                );
            }
        }

        // Формируем отчет
        DeviceInstanceReportDto report = new DeviceInstanceReportDto();
        report.setInstanceId(instanceId);
        report.setDeviceName(deviceName);
        report.setTesterName(tester.getName());
        report.setExpectedParameters(expectedParameters);
        report.setActualParameters(actualParameters);
        report.setDiscrepancies(discrepancies);

        return report;
    }
}

