package com.silveo.devices.service;

import com.silveo.devices.entity.Tester;
import com.silveo.devices.entity.User;
import com.silveo.devices.repository.TesterRepository;
import com.silveo.devices.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TesterRepository testerRepository;

    public User createUser(User user) {
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("TESTER"))) {
            if (user.getTester() == null) {
                throw new IllegalArgumentException("Для роли TESTER требуется привязка к Tester.");
            }
            // Проверяем, что Tester существует
            Tester tester = testerRepository.findById(user.getTester().getId())
                .orElseThrow(() -> new IllegalArgumentException("Tester не найден."));
            user.setTester(tester);
        }
        return userRepository.save(user);
    }
}