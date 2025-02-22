package com.silveo.devices.entity.dto;

import com.silveo.devices.entity.enums.RoleName;
import lombok.Data;

import java.util.List;

@Data
public class GetTesterDto {
    private Long id;
    private String name;
    private String username;
    private List<RoleDto> roles;
}
