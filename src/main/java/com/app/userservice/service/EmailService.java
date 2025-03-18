package com.app.userservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String senderEmail;
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;
    
    @Async
    public void sendVerificationEmail(String to, String token) throws MessagingException {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
        
        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("appName", appName);
        
        String htmlContent = templateEngine.process("email/email-verification", context);
        
        sendHtmlEmail(to, "Xác nhận email của bạn", htmlContent);
    }
    
    @Async
    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        
        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("appName", appName);
        
        String htmlContent = templateEngine.process("email/password-reset", context);
        
        sendHtmlEmail(to, "Yêu cầu đặt lại mật khẩu", htmlContent);
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        emailSender.send(message);
    }
}