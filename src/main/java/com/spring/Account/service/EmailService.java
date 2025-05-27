package com.spring.Account.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@Service
public class EmailService {
    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    public void sendAccountCreatedEmail(String toEmail, String firstName, UUID userId) throws IOException {
        String accountUrl = "http://localhost:8080/api/account/" + userId;

        String htmlContent = loadEmailTemplate(Map.of(
                "firstName", firstName,
                "appName", "AccountAPI",
                "emailAddress", "support@myapp.com",
                "name", "The AccountAPI Team",
                "accountUrl", accountUrl
        ));

        Email from = new Email("email@example.com");
        String subject = "Welcome to AccountAPI!";
        Email to = new Email(toEmail);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        sg.api(request);
    }

    private String loadEmailTemplate(Map<String, String> values) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/email.html");
        String template = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);

        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }
}
