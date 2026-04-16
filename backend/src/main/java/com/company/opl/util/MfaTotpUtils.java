package com.company.opl.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;

public final class MfaTotpUtils {

    private static final String HMAC_ALGO = "HmacSHA1";
    private static final char[] BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private MfaTotpUtils() {
    }

    public static String generateSecret() {
        byte[] buffer = new byte[20];
        new SecureRandom().nextBytes(buffer);
        return base32Encode(buffer);
    }

    public static boolean verifyCode(String base32Secret, String code, int digits, int stepSeconds, int window) {
        if (base32Secret == null || code == null || code.isBlank()) {
            return false;
        }
        long timeStep = Instant.now().getEpochSecond() / stepSeconds;
        for (int i = -window; i <= window; i++) {
            String candidate = generateCode(base32Secret, timeStep + i, digits);
            if (candidate.equals(code.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String buildOtpAuthUrl(String issuer, String account, String secret, int digits, int stepSeconds) {
        String safeIssuer = urlEncode(issuer);
        String safeAccount = urlEncode(account);
        return "otpauth://totp/" + safeIssuer + ":" + safeAccount
                + "?secret=" + secret
                + "&issuer=" + safeIssuer
                + "&algorithm=SHA1"
                + "&digits=" + digits
                + "&period=" + stepSeconds;
    }

    private static String generateCode(String base32Secret, long timeStep, int digits) {
        byte[] key = base32Decode(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("MFA code generation failed", e);
        }
    }

    private static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte value : data) {
            buffer = (buffer << 8) | (value & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                bitsLeft -= 5;
                sb.append(BASE32_CHARS[index]);
            }
        }
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            sb.append(BASE32_CHARS[index]);
        }
        return sb.toString();
    }

    private static byte[] base32Decode(String base32) {
        String normalized = base32.replace("=", "").replace(" ", "").toUpperCase();
        int buffer = 0;
        int bitsLeft = 0;
        byte[] output = new byte[normalized.length() * 5 / 8];
        int index = 0;
        for (char c : normalized.toCharArray()) {
            int val = base32CharToValue(c);
            if (val < 0) {
                continue;
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        byte[] trimmed = new byte[index];
        System.arraycopy(output, 0, trimmed, 0, index);
        return trimmed;
    }

    private static int base32CharToValue(char c) {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        }
        if (c >= '2' && c <= '7') {
            return 26 + (c - '2');
        }
        return -1;
    }

    private static String urlEncode(String value) {
        return value.replace(" ", "%20");
    }
}
