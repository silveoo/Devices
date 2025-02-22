package com.silveo.devices.controller;

import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.dto.GetTesterDto;
import com.silveo.devices.entity.dto.TesterCreationDto;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.service.TesterService;
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
public class TesterController {
    private final TesterService testerService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('testers:write')")
    public ResponseEntity<Tester> createTester(@RequestBody TesterCreationDto dto) {
        log.info("Creating new tester with username: {}", dto.getUsername());
        return testerService.createTester(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('testers:read')")
    public ResponseEntity<List<GetTesterDto>> getAllTesters() {
        log.info("Getting every tester with request");
        return ResponseEntity.ok(testerService.getAllTesters());
    }
}
