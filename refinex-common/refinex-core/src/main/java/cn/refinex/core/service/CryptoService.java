package cn.refinex.core.service;

import cn.refinex.core.autoconfigure.properties.RefinexProperties;
import cn.refinex.core.util.AesGcmUtils;
import cn.refinex.core.util.HmacUtil;

import java.util.Base64;

/**
 * 系统统一加解密服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public class CryptoService {

    /**
     * AES-GCM 加解密工具类，用于加密和解密数据。
     */
    private final AesGcmUtils aesUtil;

    /**
     * HMAC-SHA256 密钥，用于生成索引值。
     */
    private final byte[] hmacKey;

    /**
     * 构造函数，初始化 AES 密钥和 HMAC 密钥。
     *
     * @param props 配置属性，包含 AES 密钥和 HMAC 密钥
     */
    public CryptoService(RefinexProperties props) {
        byte[] aesKey = Base64.getDecoder().decode(props.getAesKey());
        this.hmacKey = Base64.getDecoder().decode(props.getHmacKey());
        this.aesUtil = new AesGcmUtils(aesKey);
    }

    /**
     * 加密明文
     *
     * @param plaintext 明文
     * @return 加密后的值，包含密文和索引值
     * @throws Exception 加密过程中可能抛出的异常
     */
    public EncryptedValue encrypt(String plaintext) throws Exception {
        String cipher = aesUtil.encrypt(plaintext);
        String index = HmacUtil.hmacSha256(hmacKey, plaintext);
        return new EncryptedValue(cipher, index);
    }

    /**
     * 解密密文
     *
     * @param cipher 密文
     * @return 明文
     * @throws Exception 解密过程中可能抛出的异常
     */
    public String decrypt(String cipher) throws Exception {
        return aesUtil.decrypt(cipher);
    }

    /**
     * 生成仅用于查询索引的 HMAC 值
     *
     * @param plaintext 明文
     * @return HMAC 索引值
     * @throws Exception 计算过程中抛出的异常
     */
    public String index(String plaintext) throws Exception {
        return HmacUtil.hmacSha256(hmacKey, plaintext);
    }

    /**
     * 加密后的值，包含密文和索引值
     */
    public record EncryptedValue(String cipher, String index) {
    }
}
