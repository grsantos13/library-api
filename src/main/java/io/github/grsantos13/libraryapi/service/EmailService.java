package io.github.grsantos13.libraryapi.service;

import java.util.List;

public interface EmailService {
    void sendMails(String mensagem, List<String> mailList);
}
