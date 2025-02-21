package com.silveo.devices.entity.embed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.silveo.devices.entity.enums.ParameterType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceParameterTemplate {
    private String name; // Например, "предельная температура"
    @Enumerated(EnumType.STRING)
    private ParameterType type; // Например, GREATER_THAN (>)
    private String value; // Значение, например "270"
    private String minValue;
    private String maxValue;
    private String tolerancePercent;
    private String allowedValues;
}