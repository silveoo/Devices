package com.silveo.devices.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceInstanceReportDto {
    private Long instanceId;
    private String deviceName;
    private String testerName;
    private Map<String, String> expectedParameters; // Ожидаемые параметры (из DeviceType)
    private Map<String, String> actualParameters;   // Фактические параметры (из DeviceInstance)
    private Map<String, String> discrepancies;     // Расхождения
}