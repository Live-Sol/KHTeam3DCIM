package com.example.KHTeam3DCIM.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    // 인증번호 이메일 발송 메서드
    public void sendEmail(String toEmail, String code) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("DCIM_Admin"); // 보내는 사람 이름 (설정 가능)
        helper.setTo(toEmail);
        helper.setSubject("[StarRoot] 비밀번호 재설정 인증번호");

        // 이메일 본문 (HTML 가능)
        String body = "<h3>요청하신 인증 번호입니다.</h3>" +
                "<h1>" + code + "</h1>" +
                "<p>인증번호를 입력창에 입력해주세요.</p>";
        helper.setText(body, true);

        javaMailSender.send(message);
    }
}