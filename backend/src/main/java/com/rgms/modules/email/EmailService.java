package com.rgms.modules.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@rgms.edu.vn}")
    private String fromAddress;

    @Async
    public void guiEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        if (!StringUtils.hasText(toEmail)) {
            log.warn("Bỏ qua gửi email vì địa chỉ người nhận rỗng. Subject={}", subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            Context context = new Context(Locale.forLanguageTag("vi-VN"));
            if (variables != null) {
                context.setVariables(variables);
            }
            String templatePath = templateName.contains("/") ? templateName : "email/" + templateName;
            String html = templateEngine.process(templatePath, context);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Không gửi được email tới {} với subject '{}': {}", toEmail, subject, ex.getMessage());
        }
    }
}
