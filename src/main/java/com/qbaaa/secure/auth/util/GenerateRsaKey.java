package com.qbaaa.secure.auth.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@UtilityClass
public class GenerateRsaKey {

    private static final String ALGORITHM = "RSA";

    public KeyPair generateRsaKey() throws NoSuchAlgorithmException {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(2048);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            log.info("Keys created.");
            return keyPair;
    }

    public RSAPrivateKey getPrivateKey(String keyData) throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] decodedKey = Base64.getDecoder().decode(keyData);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        return (RSAPrivateKey) KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec);
    }
}
