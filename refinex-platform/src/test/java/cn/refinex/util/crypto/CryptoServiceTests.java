package cn.refinex.util.crypto;

import cn.refinex.core.util.AesGcmUtils;
import cn.refinex.core.util.HmacUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;

/**
 * 加密工具测试 - 独立运行，无需 Spring 容器
 *
 * @author Refinex
 * @since 1.0.0
 */
public class CryptoServiceTests {

    /**
     * AES 加密密钥 (Base64 编码)
     */
    private static final String AES_KEY_BASE64 = "0umyw3k+P/MSrZ3FhSR81ICJzHRR7PJj8XaqH45QlkE=";

    /**
     * HMAC 密钥 (Base64 编码)
     */
    private static final String HMAC_KEY_BASE64 = "X4SCliHw1oFtAhYUeHPiRqNWn8SgiJC4D03my1uoeLA=";

    public static void main(String[] args) throws Exception {
        // 解码密钥
        byte[] aesKey = Base64.getDecoder().decode(AES_KEY_BASE64);
        byte[] hmacKey = Base64.getDecoder().decode(HMAC_KEY_BASE64);

        // 创建工具类实例
        AesGcmUtils aesUtil = new AesGcmUtils(aesKey);

        // 测试数据
        String username = "refinex";
        String nickname = "超级管理员";
        String mobile = "18688888888";
        String email = "refinex@163.com";
        String passwordPlain = "refinex.cn";

        // 1. AES-GCM 加密 + HMAC-SHA256 索引
        String mobileCipher = aesUtil.encrypt(mobile);
        String mobileIndex = HmacUtil.hmacSha256(hmacKey, mobile);

        String emailCipher = aesUtil.encrypt(email);
        String emailIndex = HmacUtil.hmacSha256(hmacKey, email);

        // 2. BCrypt 密码哈希
        String passwordHash = new BCryptPasswordEncoder(12).encode(passwordPlain);

        // 3. 打印 SQL 初始化语句
        System.out.println("=== 初始化超级管理员 SQL ===");
        System.out.printf("""
            INSERT INTO sys_user (id, username, mobile_cipher, mobile_index, email_cipher, email_index, password, nickname, sex, status, create_time)
            VALUES (1, '%s', '%s', '%s', '%s', '%s', '%s', '%s', 'MALE', 1, NOW());
            """,
                username,
                mobileCipher,
                mobileIndex,
                emailCipher,
                emailIndex,
                passwordHash,
                nickname
        );

        // 4. 验证解密
        System.out.println("\n=== 验证解密 ===");
        System.out.println("手机号密文: " + mobileCipher);
        System.out.println("解密手机号: " + aesUtil.decrypt(mobileCipher));
        System.out.println("手机号索引: " + mobileIndex);
        System.out.println();
        System.out.println("邮箱密文: " + emailCipher);
        System.out.println("解密邮箱: " + aesUtil.decrypt(emailCipher));
        System.out.println("邮箱索引: " + emailIndex);
        System.out.println();
        System.out.println("密码哈希: " + passwordHash);
        System.out.println("BCrypt 轮次: 12");

        // 5. 验证 BCrypt 密码匹配
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        boolean matches = encoder.matches(passwordPlain, passwordHash);
        System.out.println("\n=== 密码验证 ===");
        System.out.println("原始密码: " + passwordPlain);
        System.out.println("密码匹配: " + (matches ? "✅ 成功" : "❌ 失败"));
    }
}
