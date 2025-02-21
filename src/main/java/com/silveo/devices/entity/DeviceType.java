package com.silveo.devices.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.silveo.devices.entity.embed.DeviceParameterTemplate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device_type")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    private String description;

    @ElementCollection
    @CollectionTable(name = "device_type_parameters", joinColumns = @JoinColumn(name = "device_type_id"))
    private List<DeviceParameterTemplate> parameters = new ArrayList<>();
}