package cn.refinex.core.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    /**
     * 默认缓冲区大小：8KB
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 最大文件大小限制：100MB
     */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024L;

    /**
     * Tika实例，用于文件类型检测
     */
    private static final Tika TIKA = new Tika();

    /**
     * 危险文件扩展名集合
     */
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "ps1", "msi", "dll", "scr", "vbs", "js"
    );

    // ==================== 文件基础操作 ====================

    /**
     * 创建文件
     * <p>
     * 1. 如果父目录不存在，会自动创建父目录。
     * 2. 如果文件已存在，不会覆盖。
     * </p>
     *
     * @param filePath 文件路径
     * @return 是否创建成功
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static boolean createFile(String filePath) {
        validateFilePath(filePath);

        try {
            File file = new File(filePath);
            if (file.exists()) {
                log.warn("文件已存在: {}", filePath);
                return false;
            }

            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                log.error("创建父目录失败: {}", parentDir.getAbsolutePath());
                return false;
            }

            boolean created = file.createNewFile();
            if (created) {
                log.info("文件创建成功: {}", filePath);
            }

            return created;
        } catch (IOException e) {
            log.error("创建文件失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 创建目录，支持创建多级目录。
     *
     * @param dirPath 目录路径
     * @return 是否创建成功
     * @throws IllegalArgumentException 如果目录路径为空
     */
    public static boolean createDirectory(String dirPath) {
        validateFilePath(dirPath);

        try {
            Path path = Paths.get(dirPath);
            if (Files.exists(path)) {
                log.warn("目录已存在: {}", dirPath);
                return false;
            }

            Files.createDirectories(path);
            log.info("目录创建成功: {}", dirPath);

            return true;
        } catch (IOException e) {
            log.error("创建目录失败: {}", dirPath, e);
            return false;
        }
    }

    /**
     * 删除文件或目录
     * <p>
     * 如果是目录，会递归删除目录下所有文件和子目录。
     * </p>
     *
     * @param path 文件或目录路径
     * @return 是否删除成功
     * @throws IllegalArgumentException 如果路径为空
     */
    public static boolean delete(String path) {
        validateFilePath(path);

        try {
            Path targetPath = Paths.get(path);
            if (!Files.exists(targetPath)) {
                log.warn("文件或目录不存在: {}", path);
                return false;
            }

            if (Files.isDirectory(targetPath)) {
                deleteDirectoryRecursively(targetPath);
            } else {
                Files.delete(targetPath);
            }

            log.info("删除成功: {}", path);
            return true;
        } catch (IOException e) {
            log.error("删除失败: {}", path, e);
            return false;
        }
    }

    /**
     * 递归删除目录
     *
     * @param directory 目录路径
     * @throws IOException IO异常
     */
    private static void deleteDirectoryRecursively(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 复制文件，支持覆盖目标文件。
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @param overwrite  是否覆盖已存在的目标文件
     * @return 是否复制成功
     * @throws IllegalArgumentException 如果源文件或目标文件路径为空
     */
    public static boolean copyFile(String sourcePath, String targetPath, boolean overwrite) {
        validateFilePath(sourcePath);
        validateFilePath(targetPath);

        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                log.error("源文件不存在: {}", sourcePath);
                return false;
            }

            if (!Files.isRegularFile(source)) {
                log.error("源路径不是文件: {}", sourcePath);
                return false;
            }

            createParentDirectories(target);

            if (overwrite) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(source, target);
            }

            log.info("文件复制成功: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            log.error("文件复制失败: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * 移动文件或目录，相当于重命名操作。
     *
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @param overwrite  是否覆盖已存在的目标
     * @return 是否移动成功
     * @throws IllegalArgumentException 如果源路径或目标路径为空
     */
    public static boolean move(String sourcePath, String targetPath, boolean overwrite) {
        validateFilePath(sourcePath);
        validateFilePath(targetPath);

        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                log.error("源路径不存在: {}", sourcePath);
                return false;
            }

            createParentDirectories(target);

            if (overwrite) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(source, target);
            }

            log.info("移动成功: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            log.error("移动失败: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * 创建父目录
     *
     * @param path 文件路径
     * @throws IOException IO异常
     */
    private static void createParentDirectories(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    // ==================== 文件读写操作 ====================

    /**
     * 读取文件内容为字符串，使用UTF-8编码读取。
     *
     * @param filePath 文件路径
     * @return 文件内容，读取失败返回null
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static String readString(String filePath) {
        return readString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件内容为字符串
     *
     * @param filePath 文件路径
     * @param charset  字符编码
     * @return 文件内容，读取失败返回null
     * @throws IllegalArgumentException 如果文件路径为空或编码为null
     */
    public static String readString(String filePath, Charset charset) {
        validateFilePath(filePath);
        Objects.requireNonNull(charset, "字符编码不能为null");

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", filePath);
                return null;
            }
            return Files.readString(path, charset);
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param filePath 文件路径
     * @return 文件内容字节数组，读取失败返回null
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static byte[] readBytes(String filePath) {
        validateFilePath(filePath);

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", filePath);
                return null;
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 读取文件所有行
     *
     * @param filePath 文件路径
     * @return 文件行列表，读取失败返回空列表
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static List<String> readLines(String filePath) {
        return readLines(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件所有行
     *
     * @param filePath 文件路径
     * @param charset  字符编码
     * @return 文件行列表，读取失败返回空列表
     * @throws IllegalArgumentException 如果文件路径为空或编码为null
     */
    public static List<String> readLines(String filePath, Charset charset) {
        validateFilePath(filePath);
        Objects.requireNonNull(charset, "字符编码不能为null");

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", filePath);
                return Collections.emptyList();
            }
            return Files.readAllLines(path, charset);
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return Collections.emptyList();
        }
    }

    /**
     * 写入字符串到文件，使用UTF-8编码，覆盖原有内容。
     *
     * @param filePath 文件路径
     * @param content  文件内容
     * @return 是否写入成功
     * @throws IllegalArgumentException 如果文件路径或内容为空
     */
    public static boolean writeString(String filePath, String content) {
        return writeString(filePath, content, StandardCharsets.UTF_8, false);
    }

    /**
     * 写入字符串到文件
     *
     * @param filePath 文件路径
     * @param content  文件内容
     * @param charset  字符编码
     * @param append   是否追加模式
     * @return 是否写入成功
     * @throws IllegalArgumentException 如果文件路径、内容或编码为空
     */
    public static boolean writeString(String filePath, String content, Charset charset, boolean append) {
        validateFilePath(filePath);
        Objects.requireNonNull(content, "文件内容不能为null");
        Objects.requireNonNull(charset, "字符编码不能为null");

        try {
            Path path = Paths.get(filePath);
            createParentDirectories(path);

            OpenOption[] options = append
                    ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            Files.writeString(path, content, charset, options);
            log.info("文件写入成功: {}", filePath);
            return true;
        } catch (IOException e) {
            log.error("文件写入失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 写入字节数组到文件
     *
     * @param filePath 文件路径
     * @param bytes    字节数组
     * @return 是否写入成功
     * @throws IllegalArgumentException 如果文件路径或字节数组为空
     */
    public static boolean writeBytes(String filePath, byte[] bytes) {
        return writeBytes(filePath, bytes, false);
    }

    /**
     * 写入字节数组到文件
     *
     * @param filePath 文件路径
     * @param bytes    字节数组
     * @param append   是否追加模式
     * @return 是否写入成功
     * @throws IllegalArgumentException 如果文件路径或字节数组为空
     */
    public static boolean writeBytes(String filePath, byte[] bytes, boolean append) {
        validateFilePath(filePath);
        Objects.requireNonNull(bytes, "字节数组不能为null");

        try {
            Path path = Paths.get(filePath);
            createParentDirectories(path);

            OpenOption[] options = append
                    ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            Files.write(path, bytes, options);
            log.info("文件写入成功: {}", filePath);
            return true;
        } catch (IOException e) {
            log.error("文件写入失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 写入行列表到文件
     *
     * @param filePath 文件路径
     * @param lines    行列表
     * @return 是否写入成功
     * @throws IllegalArgumentException 如果文件路径或行列表为空
     */
    public static boolean writeLines(String filePath, List<String> lines) {
        return writeLines(filePath, lines, StandardCharsets.UTF_8, false);
    }

    /**
     * 写入行列表到文件
     *
     * @param filePath 文件路径
     * @param lines    行列表
     * @param charset  字符编码
     * @param append   是否追加模式
     * @return 是否写入成功
     * @throws IllegalArgumentException 如果文件路径、行列表或编码为空
     */
    public static boolean writeLines(String filePath, List<String> lines, Charset charset, boolean append) {
        validateFilePath(filePath);
        Objects.requireNonNull(lines, "行列表不能为null");
        Objects.requireNonNull(charset, "字符编码不能为null");

        try {
            Path path = Paths.get(filePath);
            createParentDirectories(path);

            OpenOption[] options = append
                    ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            Files.write(path, lines, charset, options);
            log.info("文件写入成功: {}", filePath);
            return true;
        } catch (IOException e) {
            log.error("文件写入失败: {}", filePath, e);
            return false;
        }
    }

    // ==================== 文件信息获取 ====================

    /**
     * 判断文件或目录是否存在
     *
     * @param path 文件或目录路径
     * @return 是否存在
     * @throws IllegalArgumentException 如果路径为空
     */
    public static boolean exists(String path) {
        validateFilePath(path);
        return Files.exists(Paths.get(path));
    }

    /**
     * 判断是否为文件
     *
     * @param path 路径
     * @return 是否为文件
     * @throws IllegalArgumentException 如果路径为空
     */
    public static boolean isFile(String path) {
        validateFilePath(path);
        return Files.isRegularFile(Paths.get(path));
    }

    /**
     * 判断是否为目录
     *
     * @param path 路径
     * @return 是否为目录
     * @throws IllegalArgumentException 如果路径为空
     */
    public static boolean isDirectory(String path) {
        validateFilePath(path);
        return Files.isDirectory(Paths.get(path));
    }

    /**
     * 获取文件大小（字节）
     *
     * @param filePath 文件路径
     * @return 文件大小，获取失败返回-1
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static long getFileSize(String filePath) {
        validateFilePath(filePath);

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                log.error("文件不存在或不是普通文件: {}", filePath);
                return -1L;
            }
            return Files.size(path);
        } catch (IOException e) {
            log.error("获取文件大小失败: {}", filePath, e);
            return -1L;
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filePath 文件路径
     * @return 文件扩展名（不含点号），无扩展名返回空字符串
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static String getExtension(String filePath) {
        validateFilePath(filePath);
        return FilenameUtils.getExtension(filePath);
    }

    /**
     * 获取文件名（不含扩展名）
     *
     * @param filePath 文件路径
     * @return 文件名
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static String getBaseName(String filePath) {
        validateFilePath(filePath);
        return FilenameUtils.getBaseName(filePath);
    }

    /**
     * 获取文件名（含扩展名）
     *
     * @param filePath 文件路径
     * @return 文件名
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static String getFileName(String filePath) {
        validateFilePath(filePath);
        return FilenameUtils.getName(filePath);
    }

    /**
     * 检测文件 MIME 类型
     * <p>
     * 使用 Apache Tika 进行检测，比基于扩展名的判断更准确。
     * </p>
     *
     * @param filePath 文件路径
     * @return MIME类型，检测失败返回null
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static String getMimeType(String filePath) {
        validateFilePath(filePath);

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", filePath);
                return null;
            }
            return TIKA.detect(path);
        } catch (IOException e) {
            log.error("检测MIME类型失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 根据文件名检测 MIME 类型
     * <p>
     * 使用 Apache Tika 进行检测，比基于扩展名的判断更准确。
     * </p>
     *
     * @param fileName 文件名
     * @return MIME类型，检测失败返回null
     * @throws IllegalArgumentException 如果文件名为空
     */
    public static String getMimeTypeByFileName(String fileName) {
        Assert.notNull(fileName, "文件名不能为null");
        return TIKA.detect(fileName);
    }

    /**
     * 获取文件最后修改时间
     *
     * @param filePath 文件路径
     * @return 最后修改时间，获取失败返回null
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static LocalDateTime getLastModifiedTime(String filePath) {
        validateFilePath(filePath);

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", filePath);
                return null;
            }
            return LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(path).toInstant(),
                    ZoneId.systemDefault()
            );
        } catch (IOException e) {
            log.error("获取文件修改时间失败: {}", filePath, e);
            return null;
        }
    }

    // ==================== 文件安全校验 ====================

    /**
     * 计算文件MD5哈希值
     *
     * @param filePath 文件路径
     * @return MD5哈希值（十六进制字符串），计算失败返回null
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static String calculateMd5(String filePath) {
        return calculateHash(filePath, "MD5");
    }

    /**
     * 计算文件SHA-256哈希值
     *
     * @param filePath 文件路径
     * @return SHA-256哈希值（十六进制字符串），计算失败返回null
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static String calculateSha256(String filePath) {
        return calculateHash(filePath, "SHA-256");
    }

    /**
     * 计算文件哈希值
     *
     * @param filePath  文件路径
     * @param algorithm 哈希算法（如MD5、SHA-256）
     * @return 哈希值（十六进制字符串），计算失败返回null
     * @throws IllegalArgumentException 如果文件路径或算法为空
     */
    public static String calculateHash(String filePath, String algorithm) {
        validateFilePath(filePath);
        Objects.requireNonNull(algorithm, "哈希算法不能为null");

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                log.error("文件不存在或不是普通文件: {}", filePath);
                return null;
            }

            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream inputStream = Files.newInputStream(path)) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("不支持的哈希算法: {}", algorithm, e);
            return null;
        } catch (IOException e) {
            log.error("计算文件哈希失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 验证文件是否为危险文件
     * <p>
     * 检查文件扩展名是否在危险扩展名列表中。
     * </p>
     *
     * @param filePath 文件路径
     * @return 是否为危险文件
     * @throws IllegalArgumentException 如果文件路径为空
     */
    public static boolean isDangerousFile(String filePath) {
        validateFilePath(filePath);
        String extension = getExtension(filePath).toLowerCase();
        return DANGEROUS_EXTENSIONS.contains(extension);
    }

    /**
     * 验证文件大小是否超过限制
     *
     * @param filePath 文件路径
     * @param maxSize  最大大小（字节）
     * @return 是否超过限制
     * @throws IllegalArgumentException 如果文件路径为空或最大大小小于0
     */
    public static boolean exceedsSize(String filePath, long maxSize) {
        validateFilePath(filePath);
        if (maxSize < 0) {
            throw new IllegalArgumentException("最大大小不能小于0");
        }
        long fileSize = getFileSize(filePath);
        return fileSize > maxSize;
    }

    // ==================== 文件压缩解压 ====================

    /**
     * 压缩文件或目录为ZIP格式
     *
     * @param sourcePath 源文件或目录路径
     * @param zipPath    ZIP文件输出路径
     * @return 是否压缩成功
     * @throws IllegalArgumentException 如果源路径或ZIP路径为空
     */
    public static boolean zipFile(String sourcePath, String zipPath) {
        validateFilePath(sourcePath);
        validateFilePath(zipPath);

        try {
            Path source = Paths.get(sourcePath);
            if (!Files.exists(source)) {
                log.error("源路径不存在: {}", sourcePath);
                return false;
            }

            createParentDirectories(Paths.get(zipPath));

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
                if (Files.isDirectory(source)) {
                    zipDirectory(source, source, zos);
                } else {
                    zipSingleFile(source, source.getFileName().toString(), zos);
                }
            }

            log.info("压缩成功: {} -> {}", sourcePath, zipPath);
            return true;
        } catch (IOException e) {
            log.error("压缩失败: {} -> {}", sourcePath, zipPath, e);
            return false;
        }
    }

    /**
     * 压缩目录
     *
     * @param rootPath 根路径
     * @param dirPath  当前目录路径
     * @param zos      ZIP输出流
     * @throws IOException IO异常
     */
    private static void zipDirectory(Path rootPath, Path dirPath, ZipOutputStream zos) throws IOException {
        try (Stream<Path> paths = Files.list(dirPath)) {
            for (Path path : paths.collect(Collectors.toList())) {
                String entryName = rootPath.relativize(path).toString().replace("\\", "/");
                if (Files.isDirectory(path)) {
                    zipDirectory(rootPath, path, zos);
                } else {
                    zipSingleFile(path, entryName, zos);
                }
            }
        }
    }

    /**
     * 压缩单个文件
     *
     * @param filePath  文件路径
     * @param entryName ZIP条目名称
     * @param zos       ZIP输出流
     * @throws IOException IO异常
     */
    private static void zipSingleFile(Path filePath, String entryName, ZipOutputStream zos) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zos.putNextEntry(zipEntry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    /**
     * 解压ZIP文件
     *
     * @param zipPath   ZIP文件路径
     * @param targetDir 目标目录路径
     * @return 是否解压成功
     * @throws IllegalArgumentException 如果ZIP路径或目标目录为空
     */
    public static boolean unzipFile(String zipPath, String targetDir) {
        validateFilePath(zipPath);
        validateFilePath(targetDir);

        try {
            Path zipFilePath = Paths.get(zipPath);
            if (!Files.exists(zipFilePath)) {
                log.error("ZIP文件不存在: {}", zipPath);
                return false;
            }

            Path targetDirPath = Paths.get(targetDir);
            Files.createDirectories(targetDirPath);

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path entryPath = targetDirPath.resolve(entry.getName());

                    // 安全检查：防止路径穿越攻击
                    if (!entryPath.normalize().startsWith(targetDirPath.normalize())) {
                        log.warn("检测到潜在的路径穿越攻击，跳过条目: {}", entry.getName());
                        continue;
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        createParentDirectories(entryPath);
                        try (OutputStream os = Files.newOutputStream(entryPath)) {
                            IOUtils.copy(zis, os);
                        }
                    }
                    zis.closeEntry();
                }
            }

            log.info("解压成功: {} -> {}", zipPath, targetDir);
            return true;
        } catch (IOException e) {
            log.error("解压失败: {} -> {}", zipPath, targetDir, e);
            return false;
        }
    }

    // ==================== 文件搜索过滤 ====================

    /**
     * 列出目录下所有文件
     *
     * @param dirPath   目录路径
     * @param recursive 是否递归子目录
     * @return 文件路径列表，失败返回空列表
     * @throws IllegalArgumentException 如果目录路径为空
     */
    public static List<String> listFiles(String dirPath, boolean recursive) {
        validateFilePath(dirPath);

        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.error("目录不存在或不是目录: {}", dirPath);
                return Collections.emptyList();
            }

            if (recursive) {
                try (Stream<Path> paths = Files.walk(dir)) {
                    return paths
                            .filter(Files::isRegularFile)
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            } else {
                try (Stream<Path> paths = Files.list(dir)) {
                    return paths
                            .filter(Files::isRegularFile)
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            }
        } catch (IOException e) {
            log.error("列出文件失败: {}", dirPath, e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据扩展名搜索文件
     *
     * @param dirPath    目录路径
     * @param extensions 扩展名列表（不含点号）
     * @param recursive  是否递归子目录
     * @return 匹配的文件路径列表，失败返回空列表
     * @throws IllegalArgumentException 如果目录路径或扩展名列表为空
     */
    public static List<String> findFilesByExtension(String dirPath, List<String> extensions, boolean recursive) {
        validateFilePath(dirPath);
        Objects.requireNonNull(extensions, "扩展名列表不能为null");

        if (extensions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> extensionSet = extensions.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return listFiles(dirPath, recursive).stream()
                .filter(filePath -> {
                    String ext = getExtension(filePath).toLowerCase();
                    return extensionSet.contains(ext);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据文件名模式搜索文件
     * <p>
     * 支持通配符：* 匹配任意字符，? 匹配单个字符。
     * </p>
     *
     * @param dirPath   目录路径
     * @param pattern   文件名模式（支持通配符）
     * @param recursive 是否递归子目录
     * @return 匹配的文件路径列表，失败返回空列表
     * @throws IllegalArgumentException 如果目录路径或模式为空
     */
    public static List<String> findFilesByPattern(String dirPath, String pattern, boolean recursive) {
        validateFilePath(dirPath);
        if (StrUtil.isBlank(pattern)) {
            throw new IllegalArgumentException("文件名模式不能为空");
        }

        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.error("目录不存在或不是目录: {}", dirPath);
                return Collections.emptyList();
            }

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

            if (recursive) {
                try (Stream<Path> paths = Files.walk(dir)) {
                    return paths
                            .filter(Files::isRegularFile)
                            .filter(path -> matcher.matches(path.getFileName()))
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            } else {
                try (Stream<Path> paths = Files.list(dir)) {
                    return paths
                            .filter(Files::isRegularFile)
                            .filter(path -> matcher.matches(path.getFileName()))
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            }
        } catch (IOException e) {
            log.error("搜索文件失败: {}", dirPath, e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据大小范围搜索文件
     *
     * @param dirPath   目录路径
     * @param minSize   最小大小（字节），null表示不限制
     * @param maxSize   最大大小（字节），null表示不限制
     * @param recursive 是否递归子目录
     * @return 匹配的文件路径列表，失败返回空列表
     * @throws IllegalArgumentException 如果目录路径为空
     */
    public static List<String> findFilesBySize(String dirPath, Long minSize, Long maxSize, boolean recursive) {
        validateFilePath(dirPath);

        return listFiles(dirPath, recursive).stream()
                .filter(filePath -> {
                    long size = getFileSize(filePath);
                    if (size < 0) {
                        return false;
                    }
                    if (minSize != null && size < minSize) {
                        return false;
                    }
                    return maxSize == null || size <= maxSize;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取目录大小（包含所有子文件和子目录）
     *
     * @param dirPath 目录路径
     * @return 目录大小（字节），计算失败返回-1
     * @throws IllegalArgumentException 如果目录路径为空
     */
    public static long getDirectorySize(String dirPath) {
        validateFilePath(dirPath);

        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.error("目录不存在或不是目录: {}", dirPath);
                return -1L;
            }

            try (Stream<Path> paths = Files.walk(dir)) {
                return paths
                        .filter(Files::isRegularFile)
                        .mapToLong(path -> {
                            try {
                                return Files.size(path);
                            } catch (IOException e) {
                                log.warn("无法获取文件大小: {}", path, e);
                                return 0L;
                            }
                        })
                        .sum();
            }
        } catch (IOException e) {
            log.error("计算目录大小失败: {}", dirPath, e);
            return -1L;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化文件大小为人类可读格式
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串（如：1.5 MB）
     */
    public static String formatFileSize(long size) {
        if (size < 0) {
            return "Unknown";
        }
        if (size < 1024) {
            return size + " B";
        }
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", size / Math.pow(1024, exp), pre);
    }

    /**
     * 标准化文件路径
     * <p>
     * 转换路径分隔符为系统分隔符，去除冗余的分隔符。
     * </p>
     *
     * @param path 原始路径
     * @return 标准化后的路径
     * @throws IllegalArgumentException 如果路径为空
     */
    public static String normalizePath(String path) {
        validateFilePath(path);
        return FilenameUtils.normalize(path);
    }

    /**
     * 生成唯一文件名
     * <p>
     * 如果文件已存在，在文件名后添加序号。
     * </p>
     *
     * @param dirPath  目录路径
     * @param fileName 原始文件名
     * @return 唯一文件名
     * @throws IllegalArgumentException 如果目录路径或文件名为空
     */
    public static String generateUniqueFileName(String dirPath, String fileName) {
        validateFilePath(dirPath);
        if (StrUtil.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        Path dir = Paths.get(dirPath);
        Path filePath = dir.resolve(fileName);

        if (!Files.exists(filePath)) {
            return fileName;
        }

        String baseName = getBaseName(fileName);
        String extension = getExtension(fileName);
        String extensionPart = StrUtil.isNotBlank(extension) ? "." + extension : "";

        int counter = 1;
        while (Files.exists(dir.resolve(baseName + "_" + counter + extensionPart))) {
            counter++;
        }

        return baseName + "_" + counter + extensionPart;
    }

    /**
     * 验证文件路径
     *
     * @param filePath 文件路径
     * @throws IllegalArgumentException 如果文件路径为空或空白
     */
    private static void validateFilePath(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
    }

    /**
     * 复制输入流到输出流
     *
     * @param input  输入流
     * @param output 输出流
     * @throws IOException IO异常
     */
    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        Objects.requireNonNull(input, "输入流不能为null");
        Objects.requireNonNull(output, "输出流不能为null");
        IoUtil.copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 安全关闭可关闭资源
     *
     * @param closeable 可关闭资源
     */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("关闭资源时发生异常", e);
            }
        }
    }

    /**
     * 清空目录内容（保留目录本身）
     *
     * @param dirPath 目录路径
     * @return 是否清空成功
     * @throws IllegalArgumentException 如果目录路径为空
     */
    public static boolean cleanDirectory(String dirPath) {
        validateFilePath(dirPath);

        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.error("目录不存在或不是目录: {}", dirPath);
                return false;
            }

            try (Stream<Path> paths = Files.list(dir)) {
                for (Path path : paths.collect(Collectors.toList())) {
                    if (Files.isDirectory(path)) {
                        deleteDirectoryRecursively(path);
                    } else {
                        Files.delete(path);
                    }
                }
            }

            log.info("目录清空成功: {}", dirPath);
            return true;
        } catch (IOException e) {
            log.error("清空目录失败: {}", dirPath, e);
            return false;
        }
    }

    /**
     * 比较两个文件内容是否相同
     *
     * @param filePath1 文件1路径
     * @param filePath2 文件2路径
     * @return 内容是否相同，比较失败返回false
     * @throws IllegalArgumentException 如果任一文件路径为空
     */
    public static boolean contentEquals(String filePath1, String filePath2) {
        validateFilePath(filePath1);
        validateFilePath(filePath2);

        try {
            Path path1 = Paths.get(filePath1);
            Path path2 = Paths.get(filePath2);

            if (!Files.exists(path1) || !Files.exists(path2)) {
                log.error("文件不存在: {} 或 {}", filePath1, filePath2);
                return false;
            }

            if (!Files.isRegularFile(path1) || !Files.isRegularFile(path2)) {
                log.error("路径不是文件: {} 或 {}", filePath1, filePath2);
                return false;
            }

            // 先比较文件大小
            if (Files.size(path1) != Files.size(path2)) {
                return false;
            }

            // 比较内容
            return Arrays.equals(Files.readAllBytes(path1), Files.readAllBytes(path2));
        } catch (IOException e) {
            log.error("比较文件失败: {} vs {}", filePath1, filePath2, e);
            return false;
        }
    }

}
