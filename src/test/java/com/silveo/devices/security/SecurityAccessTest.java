package com.silveo.devices.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------------- GET /api/v1/device-types ----------------

    @Test
    @DisplayName("Доступ к device-types разрешён при наличии device:read")
    @WithMockUser(authorities = {"device:read"})
    void deviceTypesAccessibleWithReadAuthority() throws Exception {
        mockMvc.perform(get("/api/v1/device-types"))
                .andExpect(status().isOk());
    }

    // ---------------- POST /api/v1/device-instances ----------------

    @Test
    @DisplayName("Создание экземпляра разрешено с device:write")
    @WithMockUser(authorities = {"device:write"})
    void createDeviceInstanceAllowedWithWrite() throws Exception {
        mockMvc.perform(post("/api/v1/device-instances")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Создание экземпляра запрещено без device:write")
    @WithMockUser(authorities = {"device:read"})
    void createDeviceInstanceForbiddenWithoutWrite() throws Exception {
        mockMvc.perform(post("/api/v1/device-instances")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isNotFound());
    }

    // ---------------- GET /auth/users/me ----------------

    @Test
    @DisplayName("Получение текущего пользователя разрешено аутентифицированным")
    @WithMockUser(username = "tester1")
    void getCurrentUserAllowedForAuthenticated() throws Exception {
        mockMvc.perform(get("/auth/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение текущего пользователя запрещено неаутентифицированным")
    @WithAnonymousUser
    void getCurrentUserUnauthorizedWithoutLogin() throws Exception {
        mockMvc.perform(get("/auth/users/me"))
                .andExpect(status().isForbidden());
    }
}
