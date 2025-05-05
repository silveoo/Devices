package com.silveo.devices.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendAccountEmail(String toEmail, String username, String password, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Уважаемый " + name + ", ваш аккаунт тестировщика создан");
        message.setText("Ваш логин: " + username + "\nВаш пароль: " + password);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            System.err.println("Ошибка отправки письма: " + e.getMessage());
        }
    }
}
