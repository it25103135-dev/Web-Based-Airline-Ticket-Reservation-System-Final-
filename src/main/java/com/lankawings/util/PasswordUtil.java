package com.lankawings.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password hashing with PBKDF2-HMAC-SHA256, a random 16-byte salt per password and 600,000 iterations
 * (OWASP recommendation). Stored format:  pbkdf2_sha256$iterations$salt$hash
 * Old unsalted SHA-256 hashes (64 hex chars) are still accepted at login and are upgraded automatically.
 */
public final class PasswordUtil {
    private PasswordUtil() {}

    private static final String PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 600_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RNG = new SecureRandom();
    private static final byte[] DUMMY_SALT = new byte[16];

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        byte[] h = pbkdf2(password, salt, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(h);
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) return false;
        try {
            if (stored.startsWith(PREFIX + "$")) {
                String[] p = stored.split("\\$");
                if (p.length != 4) return false;
                int iter = Integer.parseInt(p[1]);
                byte[] salt = Base64.getDecoder().decode(p[2]);
                byte[] expected = Base64.getDecoder().decode(p[3]);
                return MessageDigest.isEqual(expected, pbkdf2(password, salt, iter));
            }
            if (stored.length() == 64) { // legacy unsalted SHA-256 hex
                return MessageDigest.isEqual(legacySha256(password).getBytes(StandardCharsets.UTF_8),
                        stored.toLowerCase().getBytes(StandardCharsets.UTF_8));
            }
        } catch (RuntimeException e) {
            return false;
        }
        return false;
    }

    public static boolean needsUpgrade(String stored) {
        if (stored == null || !stored.startsWith(PREFIX + "$")) return true;
        try { return Integer.parseInt(stored.split("\\$")[1]) < ITERATIONS; }
        catch (RuntimeException e) { return true; }
    }

    /** Burn the same CPU time as a real check so response time doesn't reveal whether a username exists. */
    public static void dummyVerify(String password) {
        pbkdf2(password == null ? "" : password, DUMMY_SALT, ITERATIONS);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        } finally {
            spec.clearPassword();
        }
    }

    private static String legacySha256(String password) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
