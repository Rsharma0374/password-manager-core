package com.password.manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.password.manager.config.CacheConfig;
import com.password.manager.model.UserCredsCollection;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class EncryptDecryptService {

    private static final String SECRET_KEY = "SECRET_KEY";
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static String encryptText(UserCredsCollection.CredList credList, String username) throws Exception {
        String secretKey = (String) CacheConfig.CACHE.get(SECRET_KEY);
        String jsonCredList = convertCredListToJson(credList); // Convert credList to JSON string
        if (jsonCredList == null) {
            throw new Exception("Exception occurred while encrypting cred list");
        }

        byte[] salt = username.getBytes(StandardCharsets.UTF_8); // Use username as salt
        SecretKeySpec secretKeySpec = deriveKey(secretKey, salt); // Derive a key from username + secretKey

        byte[] iv = generateRandomIV();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] encryptedData = cipher.doFinal(jsonCredList.getBytes(StandardCharsets.UTF_8));

        // Combine IV + encryptedData
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private static String convertCredListToJson(UserCredsCollection.CredList credList) {
        try {
            return objectMapper.writeValueAsString(credList);
        } catch (Exception e) {
            log.error("Exception occurred while converting cred list to json with probable cause - ", e);
            return null;
        }

    }

    private static SecretKeySpec deriveKey(String secretKey, byte[] salt) throws Exception {
        byte[] keyBytes = (secretKey + new String(salt, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes); // Hash to ensure a 256-bit key
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[] generateRandomIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }


    public static String decryptText(String encryptedText, String username) {
        try {
            String secretKey = (String) CacheConfig.CACHE.get(SECRET_KEY); // Fetch from cache
            byte[] salt = username.getBytes(StandardCharsets.UTF_8); // Use username as salt
            SecretKeySpec secretKeySpec = deriveKey(secretKey, salt); // Derive a key from username + secretKey

            // Base64 decode
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extract IV and encrypted data bytes
            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] encryptedData = Arrays.copyOfRange(combined, 16, combined.length);

            // Create cipher instance for AES/CBC decryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

            // Decrypt
            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Something went wrong while decrypting", e);
            return encryptedText; // Return the encrypted text in case of failure
        }
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON to " + clazz.getSimpleName(), e);
        }
    }
}
