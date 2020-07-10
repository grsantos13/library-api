package io.github.grsantos13.libraryapi.service.impl;

import io.github.grsantos13.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${application.mail.default-from}")
    private String from;

    private final JavaMailSender javaMailSender;

    @Override
    public void sendMails(String mensagem, List<String> mailList) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(from);
            mailMessage.setSubject("Livro com empréstimo atrasado.");
            mailMessage.setText(mensagem);
        String[] listTo = mailList.toArray(new String[mailList.size()]);
        mailMessage.setTo(listTo);
        javaMailSender.send(mailMessage);
    }
}
