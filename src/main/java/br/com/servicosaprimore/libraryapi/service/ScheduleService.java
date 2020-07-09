package br.com.servicosaprimore.libraryapi.service;

import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0 12 ? * MON-FRI";

    @Value("${application.mail.lateloans.message}")
    private String mensagem;
    private final LoanService loanService;
    private final EmailService emailService;


    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendEmailsToLateLoans(){
        List<Loan> lateLoans = loanService.getAllLateLoans();
        List<String> mailList = lateLoans
                .stream()
                .map(loan -> loan.getCustomerEmail())
                .collect(Collectors.toList());



        emailService.sendMails(mensagem, mailList);
    }
}
