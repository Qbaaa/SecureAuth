package com.qbaaa.secure.auth.user.domain.service;

import com.qbaaa.secure.auth.domain.infrastructure.dto.PasswordTransferDto;
import com.qbaaa.secure.auth.user.infrastructure.entity.PasswordEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import com.qbaaa.secure.auth.user.infrastructure.repository.PasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {

  private final PasswordRepository passwordRepository;
  private final PasswordEncoder passwordEncoder;
  private final CompromisedPasswordChecker compromisedPasswordChecker;

  public void saveToUser(UserEntity userEntity, PasswordTransferDto passwordImport) {

    var passwordEntity = new PasswordEntity();
    passwordEntity.setPassword(passwordEncoder.encode(passwordImport.password()));
    passwordEntity.setUser(userEntity);
    passwordRepository.save(passwordEntity);
  }

  public void updatePassword(String newPassword, PasswordEntity passwordEntity) {

    passwordEntity.setPassword(passwordEncoder.encode(newPassword));
    passwordRepository.save(passwordEntity);
  }

  public boolean validatePassword(UserEntity user, String passwordUser) {
    var passwordEncoded = user.getPassword().getPassword();

    return passwordEncoder.matches(passwordUser, passwordEncoded);
  }

  public void validatePassword(final String password) {
    if (!password.isBlank() && compromisedPasswordChecker.check(password).isCompromised()) {
      throw new CompromisedPasswordException(
          "The password unsafe because it's well-known to hackers. "
              + "Please choose a different password that's harder to guess");
    }
  }
}
