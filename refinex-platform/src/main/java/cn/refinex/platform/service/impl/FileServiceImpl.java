package cn.refinex.platform.service.impl;

import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.exception.FileStorageException;
import cn.refinex.core.util.FileHashUtils;
import cn.refinex.core.util.ImageUtils;
import cn.refinex.core.util.StringUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.entity.SysFile;
import cn.refinex.platform.entity.SysFileData;
import cn.refinex.platform.entity.SysStorageConfig;
import cn.refinex.platform.enums.StorageType;
import cn.refinex.platform.infra.file.dto.*;
import cn.refinex.platform.infra.file.storage.FileStorageClient;
import cn.refinex.platform.infra.file.storage.FileStorageFactory;
import cn.refinex.platform.repository.SysFileDataRepository;
import cn.refinex.platform.repository.SysFileRepository;
import cn.refinex.platform.service.FileService;
import cn.refinex.platform.service.StorageConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 系统文件服务实现
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final StorageConfigService storageConfigService;
    private final FileStorageFactory storageFactory;
    private final SysFileRepository fileRepository;
    private final SysFileDataRepository fileDataRepository;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 上传文件（简单上传）
     *
     * @param file    上传的文件
     * @param options 上传选项
     * @return 文件元信息
     */
    @Override
    public SysFile upload(MultipartFile file, UploadOptions options) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        SysStorageConfig config = storageConfigService.getByCodeOrDefault(options.storageCode())
                .orElseThrow(() -> new BusinessException("未配置默认存储或指定存储编码不存在"));

        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        String ext = cn.refinex.core.util.FileUtils.getExtension(originalName);
        String mime = Objects.requireNonNullElse(file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);
        long size = file.getSize();

        try {
            byte[] bytes = file.getBytes();

            // 可选图片压缩
            if (options.imageCompress() && mime.startsWith("image/")) {
                bytes = ImageUtils.compress(bytes, options.imageMaxWidth(), options.imageQuality());
                size = bytes.length;
            }

            // 计算 SHA-256
            String sha256 = FileHashUtils.sha256Hex(new ByteArrayInputStream(bytes));

            boolean isDb = StorageType.fromCode(config.getStorageType()) == StorageType.DB;
            FileStorageClient client = storageFactory.getClient(config);

            // 组装元数据（若为 DB 存储，提前分配ID）
            SysFile sysFile = new SysFile();
            sysFile.setStorageCode(config.getStorageCode());
            sysFile.setFileName(originalName);
            sysFile.setExt(ext);
            sysFile.setMimeType(mime);
            sysFile.setSizeBytes(size);
            sysFile.setChecksumSha256(sha256);
            sysFile.setEncryptAlgo("NONE");
            sysFile.setIsDbStored(isDb ? 1 : 0);
            sysFile.setBizType(options.bizType());
            sysFile.setBizId(options.bizId());
            sysFile.setTitle(options.title());
            sysFile.setSort(0);
            sysFile.setStatus(1);
            sysFile.setCreateBy(null);
            sysFile.setCreateTime(LocalDateTime.now());
            sysFile.setUpdateBy(null);
            sysFile.setUpdateTime(LocalDateTime.now());
            sysFile.setDeleted(0);
            sysFile.setRemark(null);

            if (isDb) {
                // 先写入元数据，获取自增ID
                long newId = jdbcManager.executeInTransaction(jdbc -> fileRepository.insertAndReturnId(sysFile, jdbc));
                sysFile.setId(newId);

                // 更新 URI 和 fileKey，并写入数据
                String objectKey = "db/" + newId;
                String uri = "db://" + newId;
                byte[] finalBytes = bytes;

                jdbcManager.executeInTransaction(jdbc -> {
                    fileRepository.updateFileKeyAndUri(newId, objectKey, uri, jdbc);
                    SysFileData data = new SysFileData();
                    data.setFileId(newId);
                    data.setData(finalBytes);
                    data.setCreateTime(LocalDateTime.now());
                    data.setUpdateTime(LocalDateTime.now());
                    data.setDeleted(0);
                    fileDataRepository.insert(data, jdbc);
                    return null;
                });

                sysFile.setFileKey(objectKey);
                sysFile.setUri(uri);
            } else {
                // 先上传存储，再写入元数据
                String storedKey = client.upload(config, null, new ByteArrayInputStream(bytes), size, mime);
                java.net.URI uriObj = client.toUri(config, storedKey);
                sysFile.setFileKey(storedKey);
                sysFile.setUri(uriObj.toString());

                long newId = jdbcManager.executeInTransaction(jdbc -> fileRepository.insertAndReturnId(sysFile, jdbc));
                sysFile.setId(newId);
            }

            return sysFile;
        } catch (BusinessException be) {
            throw be;
        } catch (FileStorageException e) {
            log.error("文件上传失败(存储层异常)", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询文件元信息
     *
     * @param fileId 文件ID
     * @return 文件元信息
     */
    @Override
    public Optional<SysFile> findById(Long fileId) {
        return fileRepository.findById(fileId);
    }

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件输入流
     */
    @Override
    public FileStream download(Long fileId) {
        SysFile file = fileRepository.findById(fileId).orElseThrow(() -> new BusinessException("文件不存在"));
        SysStorageConfig config = storageConfigService.getByCodeOrDefault(file.getStorageCode())
                .orElseThrow(() -> new BusinessException("文件存储配置不存在"));

        try {
            FileStorageClient client = storageFactory.getClient(config);
            InputStream in;
            if (StorageType.fromCode(config.getStorageType()) == StorageType.DB || file.getIsDbStored() != null && file.getIsDbStored() == 1) {
                SysFileData data = fileDataRepository.findByFileId(fileId).orElseThrow(() -> new BusinessException("文件数据不存在"));
                in = new ByteArrayInputStream(data.getData());
                return new FileStream(in, file.getFileName(), file.getMimeType(), data.getData().length);
            } else {
                in = client.download(config, file.getFileKey());
                long len = file.getSizeBytes() == null ? -1 : file.getSizeBytes();
                return new FileStream(in, file.getFileName(), file.getMimeType(), len);
            }
        } catch (FileStorageException e) {
            log.error("文件下载失败(存储层异常) fileId={}", fileId, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件下载失败 fileId={}", fileId, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件（软删 + 物理删除对象）
     *
     * @param fileId     文件ID
     * @param operatorId 操作人ID
     */
    @Override
    public void delete(Long fileId, Long operatorId) {
        SysFile file = fileRepository.findById(fileId).orElseThrow(() -> new BusinessException("文件不存在"));
        SysStorageConfig config = storageConfigService.getByCodeOrDefault(file.getStorageCode())
                .orElseThrow(() -> new BusinessException("文件存储配置不存在"));

        try {
            FileStorageClient client = storageFactory.getClient(config);
            if (!(StorageType.fromCode(config.getStorageType()) == StorageType.DB || (file.getIsDbStored() != null && file.getIsDbStored() == 1))) {
                client.delete(config, file.getFileKey());
            }
        } catch (FileStorageException e) {
            log.warn("删除对象失败(存储层异常)，继续逻辑删除 fileId={} err={}", fileId, e.getMessage());
        } catch (Exception e) {
            log.warn("删除对象失败，继续逻辑删除 fileId={} err={}", fileId, e.getMessage());
        }

        jdbcManager.executeInTransaction(jdbc -> {
            fileRepository.logicalDelete(fileId, operatorId, jdbc);
            if (StorageType.fromCode(config.getStorageType()) == StorageType.DB || (file.getIsDbStored() != null && file.getIsDbStored() == 1)) {
                fileDataRepository.logicalDeleteByFileId(fileId, jdbc);
            }
            return null;
        });
    }

    /**
     * 初始化分片上传（支持 S3 兼容）
     *
     * @param request 初始化请求
     * @return 会话
     */
    @Override
    public MultipartSession initiateMultipart(MultipartInitiateRequest request) {
        SysStorageConfig config = storageConfigService.getByCodeOrDefault(request.storageCode())
                .orElseThrow(() -> new BusinessException("未配置默认存储或指定存储编码不存在"));

        FileStorageClient client = storageFactory.getClient(config);

        try {
            String objectKey = null;
            String token = client.initiateMultipartUpload(config, objectKey, request.contentType());
            // 这里 S3 实现返回的是 "objectKey|uploadId"
            String[] arr = StringUtils.split(token, "\\|");
            if (arr == null || arr.length != 2) {
                throw new BusinessException("初始化分片失败");
            }
            return new MultipartSession(config.getStorageCode(), arr[0], arr[1]);
        } catch (UnsupportedOperationException e) {
            throw new BusinessException("当前存储不支持分片上传");
        } catch (FileStorageException e) {
            log.error("初始化分片失败(存储层异常)", e);
            throw new BusinessException("初始化分片失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("初始化分片失败", e);
            throw new BusinessException("初始化分片失败: " + e.getMessage());
        }
    }

    /**
     * 上传分片
     *
     * @param request 上传分片请求
     * @return 分片元信息
     */
    @Override
    public MultipartPart uploadPart(MultipartUploadPartRequest request) {
        SysStorageConfig config = storageConfigService.getByCodeOrDefault(request.storageCode())
                .orElseThrow(() -> new BusinessException("存储配置不存在"));
        FileStorageClient client = storageFactory.getClient(config);

        try {
            String etag = client.uploadPart(config, request.objectKey(), request.uploadId(), request.partNumber(), request.input(), request.size());
            return new MultipartPart(request.partNumber(), etag);
        } catch (UnsupportedOperationException e) {
            throw new BusinessException("当前存储不支持分片上传");
        } catch (FileStorageException e) {
            log.error("上传分片失败(存储层异常)", e);
            throw new BusinessException("上传分片失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传分片失败", e);
            throw new BusinessException("上传分片失败: " + e.getMessage());
        }
    }

    /**
     * 完成分片上传
     *
     * @param request 完成分片上传请求
     * @return 文件元信息
     */
    @Override
    public SysFile completeMultipart(MultipartCompleteRequest request) {
        SysStorageConfig config = storageConfigService.getByCodeOrDefault(request.storageCode())
                .orElseThrow(() -> new BusinessException("存储配置不存在"));
        FileStorageClient client = storageFactory.getClient(config);

        try {
            client.completeMultipartUpload(config, request.objectKey(), request.uploadId(), request.etags());

            SysFile file = new SysFile();
            file.setStorageCode(config.getStorageCode());
            file.setFileKey(request.objectKey());
            file.setUri(client.toUri(config, request.objectKey()).toString());
            file.setFileName(request.fileName());
            file.setExt(cn.refinex.core.util.FileUtils.getExtension(request.fileName()));
            file.setMimeType(request.mimeType());
            file.setSizeBytes(null); // S3 HEAD 可获取，暂不请求
            file.setChecksumSha256(null);
            file.setEncryptAlgo("NONE");
            file.setIsDbStored(0);
            file.setBizType(request.bizType());
            file.setBizId(request.bizId());
            file.setTitle(request.title());
            file.setSort(0);
            file.setStatus(1);
            file.setCreateTime(LocalDateTime.now());
            file.setUpdateTime(LocalDateTime.now());
            file.setDeleted(0);

            long newId = jdbcManager.executeInTransaction(jdbc -> fileRepository.insertAndReturnId(file, jdbc));
            file.setId(newId);

            return file;
        } catch (UnsupportedOperationException e) {
            throw new BusinessException("当前存储不支持分片上传");
        } catch (FileStorageException e) {
            log.error("完成分片失败(存储层异常)", e);
            throw new BusinessException("完成分片失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("完成分片失败", e);
            throw new BusinessException("完成分片失败: " + e.getMessage());
        }
    }

    /**
     * 中止分片上传
     *
     * @param request 中止分片上传请求
     */
    @Override
    public void abortMultipart(MultipartAbortRequest request) {
        SysStorageConfig config = storageConfigService.getByCodeOrDefault(request.storageCode())
                .orElseThrow(() -> new BusinessException("存储配置不存在"));
        FileStorageClient client = storageFactory.getClient(config);

        try {
            client.abortMultipartUpload(config, request.objectKey(), request.uploadId());
        } catch (UnsupportedOperationException e) {
            // 忽略
        } catch (FileStorageException e) {
            log.warn("终止分片失败(存储层异常) {}", e.getMessage());
        } catch (Exception e) {
            log.warn("终止分片失败 {}", e.getMessage());
        }
    }

}
