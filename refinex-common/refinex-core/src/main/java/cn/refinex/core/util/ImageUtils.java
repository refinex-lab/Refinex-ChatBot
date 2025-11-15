package cn.refinex.core.util;

import cn.refinex.core.exception.FileOperationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 图片处理工具（依赖 Thumbnailator）
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageUtils {

    /**
     * 压缩图片
     *
     * @param source   原图字节
     * @param maxWidth 最大宽度（可空）
     * @param quality  输出质量 0-1（可空）
     * @return 压缩后字节
     * @throws FileOperationException 压缩失败抛出异常
     */
    public static byte[] compress(byte[] source, Integer maxWidth, Float quality) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.Builder<?> builder = Thumbnails.of(new ByteArrayInputStream(source));
            if (maxWidth != null && maxWidth > 0) {
                builder.width(maxWidth);
            }
            if (quality != null && quality > 0 && quality <= 1) {
                builder.outputQuality(quality);
            }
            builder.toOutputStream(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new FileOperationException("图片压缩失败", e);
        }
    }
}
