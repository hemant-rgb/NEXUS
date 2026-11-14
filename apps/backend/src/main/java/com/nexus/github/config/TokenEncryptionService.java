package com.nexus.github.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TokenEncryptionService {

    private static final Logger log = LoggerFactory.getLogger(TokenEncryptionService.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final GitHubProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenEncryptionService(GitHubProperties properties) {
        this.properties = properties;
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return plainText;
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            SecretKeySpec keySpec = getSecretKeySpec();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception ex) {
            log.error("Failed to encrypt token: {}", ex.getMessage());
            throw new RuntimeException("Credential encryption failed", ex);
        }
    }

    public String decrypt(String cipherTextBase64) {
        if (cipherTextBase64 == null || cipherTextBase64.isBlank()) return cipherTextBase64;
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherTextBase64);
            if (decoded.length < IV_LENGTH_BYTE) {
                return cipherTextBase64; // Fallback for raw token
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            SecretKeySpec keySpec = getSecretKeySpec();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.warn("Decryption failed, returning input (may be plain): {}", ex.getMessage());
            return cipherTextBase64;
        }
    }

    private SecretKeySpec getSecretKeySpec() {
        String key = properties.getEncryptionKey();
        byte[] keyBytes = new byte[32]; // 256 bits
        byte[] source = key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(source, 0, keyBytes, 0, Math.min(source.length, 32));
        return new SecretKeySpec(keyBytes, "AES");
    }
}
