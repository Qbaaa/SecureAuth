package com.qbaaa.secure.auth.validation;

import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Component
public class AntiSamyValidator {
  private static final String ANTISAMY_POLICY_FILE = "attackScript/antisamy.xml";

  private AntiSamy antiSamy;

  public AntiSamyValidator() throws IOException, PolicyException {
    Policy policy = Policy.getInstance(getPolicyFile());
    this.antiSamy = new AntiSamy(policy);
  }

  public boolean validate(String input) {
    try {
      return isValidInput(input);
    } catch (ScanException | PolicyException e) {
      log.error("Error validation {}", e.getMessage());
      return false;
    }
  }

  private boolean isValidInput(String input) throws ScanException, PolicyException {
    input = HtmlUtils.htmlUnescape(input);
    CleanResults scanned = antiSamy.scan(input);

    int errorsNumber = scanned.getNumberOfErrors();
    if (errorsNumber > 0) {
      log.warn("Number of errors: {}, List errors: {}", errorsNumber, scanned.getErrorMessages());
    }

    return errorsNumber < 1;
  }

  private InputStream getPolicyFile() throws IOException {
    return new ClassPathResource(ANTISAMY_POLICY_FILE).getInputStream();
  }
}
