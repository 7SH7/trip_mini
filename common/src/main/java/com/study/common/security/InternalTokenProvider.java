package com.study.common.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

public class InternalTokenProvider {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final long TOKEN_VALIDITY_SECONDS = 30;

    private final SecretKeySpec keySpec;

    public InternalTokenProvider(String secretKey) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String generateToken(String serviceName) {
        try {
            String payload = serviceName + ":" + Instant.now().getEpochSecond();

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(payload.getBytes());

            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate internal token", e);
        }
    }

    public TokenPayload validateToken(String token) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            String payload = new String(cipher.doFinal(encrypted));

            String[] parts = payload.split(":");
            if (parts.length != 2) {
                return null;
            }

            String serviceName = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            long now = Instant.now().getEpochSecond();

            if (Math.abs(now - timestamp) > TOKEN_VALIDITY_SECONDS) {
                return null;
            }

            return new TokenPayload(serviceName, timestamp);
        } catch (Exception e) {
            return null;
        }
    }

    public record TokenPayload(String serviceName, long timestamp) {}
}
