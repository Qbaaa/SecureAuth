package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.dto.PasswordTransferDto;
import com.qbaaa.secure.auth.entity.PasswordEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.repository.PasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordRepository passwordRepository;
    private final PasswordEncoder passwordEncoder;

    public void saveToUser(UserEntity userEntity, PasswordTransferDto passwordImport) {

        var passwordEntity = new PasswordEntity();
        passwordEntity.setPassword(passwordEncoder.encode(passwordImport.password()));
        passwordEntity.setUser(userEntity);
        passwordRepository.save(passwordEntity);
    }

    public boolean validatePassword(String username, String passwordUser) {
        var passwordEncoded = passwordRepository.getPasswordByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return passwordEncoder.matches(passwordUser, passwordEncoded);
    }
}
