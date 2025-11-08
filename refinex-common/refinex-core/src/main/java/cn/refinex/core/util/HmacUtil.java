package cn.refinex.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

/**
 * HMAC-SHA256 工具类，用于生成索引（可用于查询匹配）
 * <p>
 * 查询示例:
 * <pre>{@code
 * SELECT * FROM sys_user WHERE mobile_index = HMAC('18688888888');
 * }</pre>
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HmacUtil {

    /**
     * 生成 HMAC-SHA256 索引
     *
     * @param key     密钥
     * @param message 消息
     * @return HMAC-SHA256 索引
     * @throws Exception 如果生成索引失败
     */
    public static String hmacSha256(byte[] key, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        byte[] h = mac.doFinal(message.getBytes());
        return HexFormat.of().formatHex(h);
    }
}
