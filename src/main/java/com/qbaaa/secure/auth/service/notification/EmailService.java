package com.qbaaa.secure.auth.service.notification;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateService templateService;
  private final EmailProperties properties;

  @Value("classpath:/templates/mails/security-icon.png")
  private Resource iconResource;

  public void sendAccountActive(String recipient, Map<String, String> templateInputMap) {
    var text = templateService.process(properties.getActiveAccountTemplate(), templateInputMap);
    var message =
        createMessage(properties.getNotificationEmail(), recipient, "Active account", text);
    mailSender.send(message);
  }

  private MimeMessagePreparator createMessage(
      String sender, String recipient, String subject, String text) {
    return mimeMessage -> {
      var message = new MimeMessageHelper(mimeMessage, true);
      message.setFrom(sender);
      message.setTo(recipient);
      message.setSubject(subject);
      message.setText(text, true);
      message.addInline("security-icon", iconResource);
    };
  }
}
