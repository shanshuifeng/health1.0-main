
package com.healthsys.common.util;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

// 加密工具类
public class EncryptUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static SecretKeySpec secretKey;

    static {
        // 初始化密钥
        setKey("MySuperSecretKey16"); // 可以替换为从配置读取的密钥
    }

    /**
     * 设置加密密钥
     * @param myKey 原始密钥字符串
     */
    private static void setKey(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 只取前16字节作为AES-128密钥
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("初始化加密密钥失败", e);
        }
    }

    /**
     * 加密字符串
     * @param strToEncrypt 要加密的字符串
     * @return Base64编码的加密结果
     */
    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null || strToEncrypt.isEmpty()) {
            throw new IllegalArgumentException("加密内容不能为空");
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密过程中发生错误", e);
        }
    }

    /**
     * 解密字符串
     * @param strToDecrypt 要解密的Base64编码字符串
     * @return 解密后的原始字符串
     */
    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null || strToDecrypt.isEmpty()) {
            throw new IllegalArgumentException("解密内容不能为空");
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(strToDecrypt);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密过程中发生错误", e);
        }
    }

}