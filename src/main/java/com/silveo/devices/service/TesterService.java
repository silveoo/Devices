package com.silveo.devices.service;

import com.silveo.devices.entity.Role;
import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.User;
import com.silveo.devices.entity.dto.GetTesterDto;
import com.silveo.devices.entity.dto.RoleDto;
import com.silveo.devices.entity.dto.TesterCreationDto;
import com.silveo.devices.entity.enums.RoleName;
import com.silveo.devices.repository.RoleRepository;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.repository.UserRepository;
import com.silveo.devices.entity.Authority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TesterService {
    private final TesterRepository testerRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final MailService mailService;

    public ResponseEntity<Tester> createTester(TesterCreationDto dto) {

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName(RoleName.TESTER)));
        userRepository.save(user);

        Tester tester = new Tester();
        tester.setName(dto.getName());
        tester.setUser(user);
        testerRepository.save(tester);

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            mailService.sendAccountEmail(dto.getEmail(), dto.getUsername(), dto.getPassword());
        }

        return ResponseEntity.ok(tester);
    }

    public List<GetTesterDto> getAllTesters() {
        List<Tester> testers = testerRepository.findAllWithUserRolesAndAuthorities();

        return testers.stream().map(tester -> {
            GetTesterDto dto = new GetTesterDto();
            dto.setId(tester.getId());
            dto.setName(tester.getName());

            if (tester.getUser() != null) {
                User user = tester.getUser();
                dto.setUsername(user.getUsername());

                // Преобразование ролей в DTO
                List<RoleDto> roleDtos = user.getRoles().stream()
                        .map(role -> {
                            RoleDto roleDto = new RoleDto();
                            roleDto.setName(role.getName());
                            roleDto.setAuthorities(
                                    role.getAuthorities().stream()
                                            .map(Authority::getName)
                                            .collect(Collectors.toList())
                            );
                            return roleDto;
                        })
                        .collect(Collectors.toList());

                dto.setRoles(roleDtos);
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
