package com.silveo.devices.controller;

import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.dto.TesterCreationDto;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.service.TesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/testers")
@RequiredArgsConstructor
public class TesterController {
    private final TesterRepository repository;
    private final TesterService testerService;

    @PostMapping
    public ResponseEntity<Tester> createTester(@RequestBody TesterCreationDto dto) {
        return testerService.createTester(dto);
    }

    @GetMapping
    public ResponseEntity<List<Tester>> getAllTesters() {
        return ResponseEntity.ok(repository.findAll());
    }
}
