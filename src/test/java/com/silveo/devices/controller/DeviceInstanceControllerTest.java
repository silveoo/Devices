package com.silveo.devices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silveo.devices.config.TestSecurityConfig;
import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.dto.DeviceInstanceRequestDto;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.service.DeviceInstanceService;
import com.silveo.devices.service.PdfReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceInstanceController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = {DeviceInstanceController.class})
class DeviceInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceInstanceRepository repository;

    @MockBean
    private DeviceInstanceService deviceInstanceService;

    @MockBean
    private PdfReportService pdfReportService;

    @Test
    @WithMockUser(authorities = "device:write")
    @DisplayName("Создание экземпляра устройства с правами device:write")
    void createDeviceInstance_shouldSucceed() throws Exception {
        DeviceInstanceRequestDto requestDto = new DeviceInstanceRequestDto();
        requestDto.setDeviceName("Thermometer");
        requestDto.setRequestId("req-123");
        requestDto.setTesterId(1L);

        Map<String, Object> response = Map.of(
                "instance", new DeviceInstance(),
                "valid", true
        );

        Mockito.when(deviceInstanceService.createInstance(any(DeviceInstanceRequestDto.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok(response)); // без generic

        mockMvc.perform(post("/api/v1/device-instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}
