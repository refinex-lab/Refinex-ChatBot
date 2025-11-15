package cn.refinex.platform.service;

import cn.refinex.platform.entity.SysFile;
import cn.refinex.platform.infra.file.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * 系统文件服务（统一入口）
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface FileService {

    /**
     * 上传文件（简单上传）
     *
     * @param file    上传的文件
     * @param options 上传选项
     * @return 文件元信息
     */
    SysFile upload(MultipartFile file, UploadOptions options);

    /**
     * 根据ID查询文件元信息
     *
     * @param fileId 文件ID
     * @return 文件元信息
     */
    Optional<SysFile> findById(Long fileId);

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件输入流
     */
    FileStream download(Long fileId);

    /**
     * 删除文件（软删 + 物理删除对象）
     *
     * @param fileId     文件ID
     * @param operatorId 操作人ID
     */
    void delete(Long fileId, Long operatorId);

    /**
     * 初始化分片上传（支持 S3 兼容）
     *
     * @param request 初始化请求
     * @return 会话
     */
    MultipartSession initiateMultipart(MultipartInitiateRequest request);

    /**
     * 上传分片
     *
     * @param request 上传分片请求
     * @return 分片元信息
     */
    MultipartPart uploadPart(MultipartUploadPartRequest request);

    /**
     * 完成分片上传
     *
     * @param request 完成分片上传请求
     * @return 文件元信息
     */
    SysFile completeMultipart(MultipartCompleteRequest request);

    /**
     * 中止分片上传
     *
     * @param request 中止分片上传请求
     */
    void abortMultipart(MultipartAbortRequest request);
}
