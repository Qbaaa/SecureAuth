package com.qbaaa.secure.auth.user.domain.service;

import com.qbaaa.secure.auth.multifactorauth.domain.MfaProvider;
import com.qbaaa.secure.auth.shared.config.otp.OtpProvider;
import com.qbaaa.secure.auth.shared.config.time.TimeProvider;
import com.qbaaa.secure.auth.user.domain.enums.MfaType;
import com.qbaaa.secure.auth.user.domain.enums.OperationType;
import com.qbaaa.secure.auth.user.infrastructure.entity.OtpEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import com.qbaaa.secure.auth.user.infrastructure.repository.OptRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

  private final TimeProvider timeProvider;
  private final OptRepository optRepository;
  private final OtpProvider otpProvider;
  private final List<MfaProvider> mfaProviders;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void triggerOtp(UserEntity user, OperationType operationType) {
    final OtpEntity otp = createToken(user, operationType);
    otp.setUser(user);
    sendOtp(user.getMultifactorAuthType(), user.getEmail(), otp.getSecret());
  }

  private OtpEntity createToken(UserEntity user, OperationType operationType) {
    var opt = new OtpEntity();
    opt.setOperationType(operationType);
    opt.setSecret(otpProvider.generateSecret());
    opt.setCreatedAt(timeProvider.getLocalDateTimeNow());
    opt.setUser(user);

    return optRepository.save(opt);
  }

  private void sendOtp(MfaType type, String recipient, String otp) {
    mfaProviders.stream()
        .filter(t -> t.supports(type))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No provider found for type" + type))
        .sendOtp(recipient, otp);
  }
}
