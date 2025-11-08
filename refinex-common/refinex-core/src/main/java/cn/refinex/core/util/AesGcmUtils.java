package cn.refinex.core.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM 加解密工具类
 *
 * @author Refinex
 * @since 1.0.0
 */
public class AesGcmUtils {

    /**
     * AES-GCM 推荐使用 12 字节的 IV（Initialization Vector）。
     */
    private static final int IV_LENGTH = 12;

    /**
     * AES-GCM 推荐使用 128 位的认证标签（TAG）。
     */
    private static final int TAG_LENGTH_BIT = 128;

    /**
     * AES 密钥，必须是 32 字节（256 位）长度。
     */
    private final byte[] key;

    /**
     * 随机数生成器，用于生成 IV。
     */
    private static final SecureRandom RNG = new SecureRandom();

    /**
     * 构造函数，初始化 AES 密钥。
     *
     * @param key 32 字节（256 位）的 AES 密钥
     */
    public AesGcmUtils(byte[] key) {
        if (key.length != 32) {
            throw new IllegalArgumentException("AES key must be 32 bytes (256-bit).");
        }
        this.key = key;
    }

    /**
     * 加密方法，使用 AES-GCM 模式加密明文。
     *
     * @param plaintext 待加密的明文
     * @return 加密后的 Base64 编码字符串，包含 IV 和密文
     * @throws Exception 如果加密过程中发生错误
     */
    public String encrypt(String plaintext) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        RNG.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
        byte[] ct = cipher.doFinal(plaintext.getBytes());
        byte[] result = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ct, 0, result, iv.length, ct.length);
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 解密方法，使用 AES-GCM 模式解密密文。
     *
     * @param base64Ciphertext 包含 IV 和密文的 Base64 编码字符串
     * @return 解密后的明文
     * @throws Exception 如果解密过程中发生错误
     */
    public String decrypt(String base64Ciphertext) throws Exception {
        byte[] input = Base64.getDecoder().decode(base64Ciphertext);
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(input, 0, iv, 0, IV_LENGTH);
        byte[] ct = new byte[input.length - IV_LENGTH];
        System.arraycopy(input, IV_LENGTH, ct, 0, ct.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
        byte[] pt = cipher.doFinal(ct);
        return new String(pt);
    }
}
