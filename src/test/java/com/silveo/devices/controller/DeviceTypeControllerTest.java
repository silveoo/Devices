package com.silveo.devices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.repository.DeviceTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {DeviceTypeController.class})
@WebMvcTest(DeviceTypeController.class)
class DeviceTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceTypeRepository deviceTypeRepository;

    @Test
    @WithMockUser(authorities = "device:read")
    @DisplayName("Получение типа устройства по id")
    void getDeviceTypeById_shouldReturnDeviceType() throws Exception {
        DeviceType type = new DeviceType();
        type.setId(1L);
        type.setName("Thermometer");

        Mockito.when(deviceTypeRepository.findById(1L))
                .thenReturn(Optional.of(type));

        mockMvc.perform(get("/api/v1/device-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Thermometer"));
    }

    @Test
    @WithMockUser(authorities = "devicetype:write")
    @DisplayName("Создание типа устройства с правами devicetype:write")
    void createDeviceType_shouldSucceed() throws Exception {
        DeviceType request = new DeviceType();
        request.setName("Thermometer");

        DeviceType saved = new DeviceType();
        saved.setId(1L);
        saved.setName("Thermometer");

        Mockito.when(deviceTypeRepository.save(any(DeviceType.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/device-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Thermometer"));
    }

}
