package com.silveo.devices.entity.dto;

import com.silveo.devices.entity.embed.DeviceParameter;
import lombok.Data;

import java.util.List;

@Data
public class DeviceInstanceRequestDto {
    private String requestId;
    private String deviceName;
    private Long testerId;
    private List<DeviceParameter> parameters;
}
