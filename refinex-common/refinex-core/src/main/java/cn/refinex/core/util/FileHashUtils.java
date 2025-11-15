package cn.refinex.core.util;

import cn.refinex.core.exception.FileOperationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 文件 Hash 工具
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileHashUtils {

    /**
     * 计算 SHA-256
     *
     * @param in 输入流
     * @return SHA-256
     * @throws FileOperationException 文件操作异常
     */
    public static String sha256Hex(InputStream in) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(in, md); OutputStream os = new NullOutputStream()) {
                dis.transferTo(os);
            }
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new FileOperationException("计算文件 SHA-256 失败", e);
        }
    }

    /**
     * 空输出流
     */
    private static final class NullOutputStream extends OutputStream {

        /**
         * 写入
         *
         * @param b 字节
         */
        @Override
        public void write(int b) {
            // 忽略
        }

        /**
         * 写入
         *
         * @param b   字节
         * @param off 偏移
         * @param len 长度
         */
        @NullMarked
        @Override
        public void write(byte[] b, int off, int len) {
            // 忽略
        }
    }
}
