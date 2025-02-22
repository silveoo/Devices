package com.silveo.devices.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Tester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "user_id") // Внешний ключ в таблице Tester
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "tester")
    @JsonIgnore
    private List<DeviceInstance> testedDevices = new ArrayList<>();
}
