package com.qbaaa.secure.auth.shared.email;

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

  @Value("classpath:/templates/mails/security-icon.png")
  private Resource iconResource;

  public void send(
      String sender,
      String recipient,
      String subject,
      String template,
      Map<String, String> templateInputMap) {
    var text = templateService.process(template, templateInputMap);
    var message = createMessage(sender, recipient, subject, text);
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
