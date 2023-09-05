package ua.desktop.chat.messenger.auth.dao.util;

import ua.desktop.chat.messenger.auth.domain.exceptions.DomainException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption{
    /**
     * A method for encrypting a password using the SHA3-256 algorithm.
     *
     * @param password a string representing the password to be encrypted.
     * @return the encrypted password as a string with hexadecimal values.
     * @throws DomainException if an error occurs during encryption.
     */
    public static String encryptionSHA3256(String password) throws DomainException {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            final byte[] hashbytes = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashbytes);
        } catch (NoSuchAlgorithmException e) {
            throw new DomainException("Cryptographic algorithm isn't available!", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
