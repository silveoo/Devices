package com.silveo.devices.integration;

import com.silveo.devices.entity.DeviceInstance;
import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.User;
import com.silveo.devices.entity.embed.DeviceParameter;
import com.silveo.devices.entity.embed.DeviceParameterTemplate;
import com.silveo.devices.entity.enums.ParameterType;
import com.silveo.devices.repository.DeviceInstanceRepository;
import com.silveo.devices.repository.DeviceTypeRepository;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.repository.UserRepository;
import com.silveo.devices.service.DeviceValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeviceIntegrationTest {

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private DeviceInstanceRepository deviceInstanceRepository;

    @Autowired
    private TesterRepository testerRepository;

    @Autowired
    private DeviceValidationService deviceValidationService;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void cleanDb() {
        deviceInstanceRepository.deleteAll();
        deviceTypeRepository.deleteAll();
    }

    @Test
    @Transactional
    void testDeviceInstanceValidationWithDefects() {
        DeviceType deviceType = new DeviceType();
        deviceType.setName("Thermometer");

        DeviceParameterTemplate template = new DeviceParameterTemplate();
        template.setName("Температура");
        template.setType(ParameterType.RANGE);
        template.setMinValue("0");
        template.setMaxValue("100");

        deviceType.setParameters(List.of(template));
        deviceTypeRepository.save(deviceType);

        Tester tester = new Tester();
        tester.setName("Тестировщик");
        testerRepository.save(tester);

        DeviceParameter param1 = new DeviceParameter();
        param1.setName("Температура");
        param1.setValue("42");

        DeviceInstance correct = new DeviceInstance();
        correct.setDeviceType(deviceType);
        correct.setTester(tester);
        correct.setParameters(List.of(param1));

        boolean valid1 = deviceValidationService.validate(deviceType, correct);
        correct.setAnyDefects(!valid1);

        deviceInstanceRepository.save(correct);

        DeviceParameter param2 = new DeviceParameter();
        param2.setName("Температура");
        param2.setValue("200");

        DeviceInstance faulty = new DeviceInstance();
        faulty.setDeviceType(deviceType);
        faulty.setTester(tester);
        faulty.setParameters(List.of(param2));

        boolean valid2 = deviceValidationService.validate(deviceType, faulty);
        faulty.setAnyDefects(!valid2);

        deviceInstanceRepository.save(faulty);

        DeviceInstance loadedCorrect = deviceInstanceRepository.findById(correct.getId()).orElseThrow();
        DeviceInstance loadedFaulty = deviceInstanceRepository.findById(faulty.getId()).orElseThrow();

        assertFalse(Boolean.TRUE.equals(loadedCorrect.getAnyDefects()));
        assertTrue(Boolean.TRUE.equals(loadedFaulty.getAnyDefects()));
    }
}
