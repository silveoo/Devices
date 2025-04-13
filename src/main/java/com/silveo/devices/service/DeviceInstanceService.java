package com.silveo.devices.service;

import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.User;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.entity.dto.DeviceInstanceRequestDto;
import com.silveo.devices.entity.embed.DeviceParameter;
import com.silveo.devices.entity.embed.DeviceParameterTemplate;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.repository.DeviceTypeRepository;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
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
    private final UserRepository userRepository;

    public ResponseEntity<?> createInstance(DeviceInstanceRequestDto request) throws AccessDeniedException {
        DeviceType type = typeRepository.findByName(request.getDeviceName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device type not found"));
        Tester currentTester = getCurrentTester();

        DeviceInstance instance = new DeviceInstance();
        instance.setDeviceType(type);
        instance.setParameters(request.getParameters());
        instance.setTester(currentTester);


        boolean isValid = deviceValidationService.validate(type, instance);
        instance.setAnyDefects(!isValid);
        repository.save(instance);

        return ResponseEntity.ok(Map.of("instance", instance, "valid", isValid));
    }

    public DeviceInstanceReportDto generateReport(Long instanceId, String deviceName) {
        DeviceInstance instance = repository.findById(instanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device instance not found"));
        DeviceType type = typeRepository.findByName(deviceName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device type not found"));
        Tester tester = instance.getTester();

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
                            case BOOLEAN -> "Должно быть: " + template.getValue();
                            case NOT_EQUALS -> "Не должно быть: " + template.getValue();
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

        DeviceInstanceReportDto report = new DeviceInstanceReportDto();
        report.setInstanceId(instanceId);
        report.setDeviceName(deviceName);
        report.setTesterName(tester.getName());
        report.setExpectedParameters(expectedParameters);
        report.setActualParameters(actualParameters);
        report.setDiscrepancies(discrepancies);

        return report;
    }

    private Tester getCurrentTester() throws AccessDeniedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        if (user.getTester() == null) {
            throw new AccessDeniedException("Current user is not a tester");
        }
        return user.getTester();
    }
}

