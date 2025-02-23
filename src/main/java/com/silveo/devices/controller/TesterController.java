package com.silveo.devices.controller;

import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.dto.GetTesterDto;
import com.silveo.devices.entity.dto.TesterCreationDto;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.service.TesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/testers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tester", description = "Управление тестировщиками")
public class TesterController {
    private final TesterService testerService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('testers:write')")
    @Operation(summary = "Создание тестировщика",
            description = "Создание тестировщика, задание ему логина и пароля для будущей аутентификации. Роль TESTER")
    public ResponseEntity<Tester> createTester(@RequestBody TesterCreationDto dto) {
        log.info("Creating new tester with username: {}", dto.getUsername());
        return testerService.createTester(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('testers:read')")
    @Operation(summary = "Получение всех тестировщиков", description = "Получение всех тестировщиков, их ролей и прав")
    public ResponseEntity<List<GetTesterDto>> getAllTesters() {
        log.info("Getting every tester with request");
        return ResponseEntity.ok(testerService.getAllTesters());
    }
}
