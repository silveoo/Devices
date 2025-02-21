package com.silveo.devices.service;

import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.dto.TesterCreationDto;
import com.silveo.devices.repository.TesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TesterService {
    private final TesterRepository testerRepository;

    public ResponseEntity<Tester> createTester(TesterCreationDto dto) {
        Tester tester = new Tester();
        tester.setName(dto.getName());
        testerRepository.save(tester);
        return ResponseEntity.ok(tester);
    }
}
