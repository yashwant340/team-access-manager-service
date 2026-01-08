package com.example.accessManager.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendApprovalEmail(String to, String username, String tempPassword, String loginUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("[Team Access Manager] Your Access Request Approved");

            String htmlContent = String.format("""
                <p>Hello,</p>
                <p>Your request to access the Team Access Manager has been approved.</p>
                <p><b>Username:</b> %s<br>
                <b>Temporary Password:</b> %s</p>
                <p>You can log in here: <a href="%s">%s</a></p>
                <p><b>Note:</b> Please change your password immediately after logging in.</p>
                <p>Thanks,<br>Admin Team</p>
            """, username, tempPassword, loginUrl, loginUrl);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending approval email", e);
        }
    }

    public void sendOtpEmail(String to, String otp) throws MessagingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Your OTP Code");
        helper.setText("Your OTP for password reset is: " + otp + "\nIt expires in 5 minutes.");

        mailSender.send(message);
    }
}
