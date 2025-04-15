package com.qbaaa.secure.auth.event;

import com.qbaaa.secure.auth.notification.EmailService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountActiveListener {

  private final EmailService emailService;

  @Value("${secureauth.mail.activationPath}")
  private String activationPathTemplate;

  @EventListener
  @Async
  public void onSendEmail(AccountActiveEvent accountActiveEvent) {
    var activationLink =
        generateLink(
            accountActiveEvent.getBaseUrl(),
            accountActiveEvent.getDomainName(),
            accountActiveEvent.getToken());
    var templateInputMap =
        Map.of(
            "nickname", accountActiveEvent.getUsername(),
            "domainName", accountActiveEvent.getDomainName(),
            "activationLink", activationLink);
    emailService.sendAccountActive(accountActiveEvent.getEmail(), templateInputMap);
  }

  private String generateLink(String baseUrl, String domainName, String token) {
    var activationPath = String.format(activationPathTemplate, domainName, token);

    return baseUrl + activationPath;
  }
}
