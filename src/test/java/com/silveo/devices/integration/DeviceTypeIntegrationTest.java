package com.silveo.devices.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silveo.devices.entity.DeviceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class DeviceTypeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Интеграционный: создание типа устройства")
    @WithMockUser(authorities = "devicetype:write")
    void createDeviceType_success() throws Exception {
        DeviceType deviceType = new DeviceType();
        deviceType.setName("Thermometer2");

        mockMvc.perform(post("/api/v1/device-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Thermometer2"));
    }
}
