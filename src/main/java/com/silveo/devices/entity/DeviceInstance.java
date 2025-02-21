package com.silveo.devices.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.silveo.devices.entity.embed.DeviceParameter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device_instance")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tester_id", nullable = false)
    private Tester tester;

    @ManyToOne
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    @ElementCollection
    @CollectionTable(name = "device_instance_parameters", joinColumns = @JoinColumn(name = "device_instance_id"))
    private List<DeviceParameter> parameters = new ArrayList<>();
}
