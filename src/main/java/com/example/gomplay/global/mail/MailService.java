package com.example.gomplay.global.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[Gomplay] 이메일 인증 코드");
            helper.setText(buildEmailContent(token), true);  // true = HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }

    private String buildEmailContent(String token) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #333;">Gomplay 이메일 인증</h2>
                    <p>아래 인증 코드를 입력해주세요.</p>
                    <div style="background-color: #f4f4f4; padding: 20px; text-align: center;
                                font-size: 32px; font-weight: bold; letter-spacing: 8px;">
                        %s
                    </div>
                    <p style="color: #888;">인증 코드는 5분 후 만료됩니다.</p>
                </div>
                """.formatted(token);
    }
}