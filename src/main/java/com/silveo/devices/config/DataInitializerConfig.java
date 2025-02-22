package com.silveo.devices.config;

import com.silveo.devices.entity.Authority;
import com.silveo.devices.entity.Role;
import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.User;
import com.silveo.devices.entity.enums.RoleName;
import com.silveo.devices.repository.AuthorityRepository;
import com.silveo.devices.repository.RoleRepository;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializerConfig {

    private final UserRepository userRepo;
    private final TesterRepository testerRepo;
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            if(userRepo.count() == 0) {
                // Создаем права (authorities)
                Authority deviceRead = new Authority();
                deviceRead.setName("device:read");

                Authority deviceWrite = new Authority();
                deviceWrite.setName("device:write");

                Authority reportGenerate = new Authority();
                reportGenerate.setName("report:generate");

                Authority testersRead = new Authority();
                testersRead.setName("testers:read");

                Authority testersWrite = new Authority();
                testersWrite.setName("testers:write");

                Authority deviceTypeWrite = new Authority();
                deviceTypeWrite.setName("devicetype:write");

                authorityRepository.saveAll(List.of(deviceRead, deviceWrite, reportGenerate, testersRead, testersWrite, deviceTypeWrite));

                Role adminRole = new Role();
                adminRole.setName(RoleName.ADMIN);
                adminRole.setAuthorities(new HashSet<>(List.of(deviceRead, deviceWrite, reportGenerate, testersRead, testersWrite, deviceTypeWrite)));

                Role testerRole = new Role();
                testerRole.setName(RoleName.TESTER);
                testerRole.setAuthorities(new HashSet<>(List.of(deviceWrite, reportGenerate, deviceRead)));

                Role botRole = new Role();
                botRole.setName(RoleName.BOT);
                botRole.setAuthorities(new HashSet<>(List.of(reportGenerate, deviceRead)));

                roleRepository.saveAll(List.of(adminRole, testerRole, botRole));

                User user = new User();
                user.setUsername("tester1");
                user.setPassword(encoder.encode("password"));
                user.setRoles(Set.of(testerRole));
                user = userRepo.save(user);

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("adminpass"));
                admin.setRoles(Set.of(adminRole));
                userRepo.save(admin);

                Tester tester = new Tester();
                tester.setName("Иван Петров");
                tester.setUser(user);
                tester = testerRepo.save(tester);

                Tester testerAdmin = new Tester();
                testerAdmin.setName("Администратор");
                testerAdmin.setUser(admin);
                testerAdmin = testerRepo.save(testerAdmin);

                user.setTester(tester);
                userRepo.save(user);
            }
        };
    }
}


