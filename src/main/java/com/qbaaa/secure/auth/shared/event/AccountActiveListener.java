package com.qbaaa.secure.auth.shared.event;

import com.qbaaa.secure.auth.shared.email.ActiveAccountEmailProperties;
import com.qbaaa.secure.auth.shared.email.EmailService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountActiveListener {

  private final EmailService emailService;
  private final ActiveAccountEmailProperties properties;

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
    emailService.send(
        properties.getSender(),
        accountActiveEvent.getEmail(),
        "Active account",
        properties.getTemplate(),
        templateInputMap);
  }

  private String generateLink(String baseUrl, String domainName, String token) {
    var activationPath = String.format(properties.getPath(), domainName, token);

    return baseUrl + activationPath;
  }
}
