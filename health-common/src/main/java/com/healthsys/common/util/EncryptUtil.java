package com.healthsys.common.util;

/**
 * 密码工具类 — 明文存储模式
 * encrypt/decrypt 为透传，可随时切换回 AES 加密
 */
public class EncryptUtil {

    public static String encrypt(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        return str; // 透传明文
    }

    public static String decrypt(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        return str; // 透传明文
    }
}
