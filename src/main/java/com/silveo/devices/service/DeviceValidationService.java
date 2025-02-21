package com.silveo.devices.service;

import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.entity.embed.DeviceParameterTemplate;
import com.silveo.devices.entity.embed.DeviceParameter;
import com.silveo.devices.entity.enums.ParameterType;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.repository.DeviceTypeRepository;
import com.silveo.devices.repository.TesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceValidationService {

    public boolean validate(DeviceType type, DeviceInstance instance) {
        Map<String, String> actualParams = instance.getParameters().stream()
                .collect(Collectors.toMap(DeviceParameter::getName, DeviceParameter::getValue));

        for (DeviceParameterTemplate template : type.getParameters()) {
            String actualValue = actualParams.get(template.getName());
            if (actualValue == null) return false;

            if (!validateParameter(template, actualValue)) {
                return false;
            }
        }
        return true;
    }

    public boolean validateParameter(DeviceParameterTemplate template, String actualValue) {
        try {
            return switch (template.getType()) {
                case GREATER_THAN -> Double.parseDouble(actualValue) > Double.parseDouble(template.getValue());
                case LESS_THAN -> Double.parseDouble(actualValue) < Double.parseDouble(template.getValue());
                case EQUALS -> Double.parseDouble(actualValue) == Double.parseDouble(template.getValue());
                case EQUALS_STRING -> actualValue.equals(template.getValue());
                case RANGE -> validateRange(actualValue, template);
                case DEVIATION -> validateDeviation(actualValue, template);
                case ENUM -> validateEnum(actualValue, template);
            };
        } catch (NumberFormatException e) {
            return false; // Если что-то пошло не так, значение невалидное
        }
    }

    private boolean validateRange(String actualValue, DeviceParameterTemplate template) {
        double actual = Double.parseDouble(actualValue);
        double min = Double.parseDouble(template.getMinValue());
        double max = Double.parseDouble(template.getMaxValue());
        return actual >= min && actual <= max;
    }

    private boolean validateDeviation(String actualValue, DeviceParameterTemplate template) {
        double actual = Double.parseDouble(actualValue);
        double expected = Double.parseDouble(template.getValue());
        double tolerance = expected * (Double.parseDouble(template.getTolerancePercent()) / 100);
        return actual >= (expected - tolerance) && actual <= (expected + tolerance);
    }

    private boolean validateEnum(String actualValue, DeviceParameterTemplate template) {
        if (template.getAllowedValues() == null || template.getAllowedValues().isEmpty()) {
            return false;
        }

        List<String> allowed = Arrays.asList(template.getAllowedValues().split("\\s*,\\s*"));
        return allowed.contains(actualValue.trim());
    }
}
