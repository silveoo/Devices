package com.silveo.devices.entity.dto;

import com.silveo.devices.entity.enums.RoleName;
import lombok.Data;

import java.util.List;

@Data
public class RoleDto {
    private RoleName name;
    private List<String> authorities;
}