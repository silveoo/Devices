package com.silveo.devices.entity.embed;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceParameter {
    private String name;  // Например, "предельная температура"
    private String value; // Например, "271"
}