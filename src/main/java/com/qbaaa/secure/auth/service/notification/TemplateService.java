package com.qbaaa.secure.auth.service.notification;

import com.qbaaa.secure.auth.config.MailConfig;
import com.qbaaa.secure.auth.exception.SendEmailException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
@RequiredArgsConstructor
public class TemplateService {

  private final MailConfig mailConfig;

  public String process(String templateName, Map<String, String> templateInputMap) {
    try {
      var template = mailConfig.freemarkerClassLoaderConfig().getTemplate(templateName);
      return FreeMarkerTemplateUtils.processTemplateIntoString(template, templateInputMap);

    } catch (Exception e) {
      throw new SendEmailException("Sending email failed");
    }
  }
}
