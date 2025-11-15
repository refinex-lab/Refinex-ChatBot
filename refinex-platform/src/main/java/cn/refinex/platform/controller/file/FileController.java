package cn.refinex.platform.controller.file;

import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.platform.controller.file.dto.request.MultipartAbortRequestDTO;
import cn.refinex.platform.controller.file.dto.request.MultipartCompleteRequestDTO;
import cn.refinex.platform.controller.file.dto.request.MultipartInitiateRequestDTO;
import cn.refinex.platform.entity.SysFile;
import cn.refinex.platform.infra.file.dto.*;
import cn.refinex.platform.service.FileService;
import cn.refinex.satoken.common.helper.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 系统文件接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "系统文件管理")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameters(
            value = {
                    @Parameter(name = "file", description = "文件", required = true),
                    @Parameter(name = "storageCode", description = "存储编码"),
                    @Parameter(name = "bizType", description = "业务类型"),
                    @Parameter(name = "bizId", description = "业务ID"),
                    @Parameter(name = "title", description = "标题"),
                    @Parameter(name = "compress", description = "是否压缩"),
            }
    )
    public ApiResponse<SysFile> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "bizId", required = false) String bizId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "compress", required = false, defaultValue = "false") boolean compress,
            @RequestParam(value = "maxWidth", required = false) Integer maxWidth,
            @RequestParam(value = "quality", required = false) Float quality
    ) {
        UploadOptions options = new UploadOptions(storageCode, bizType, bizId, title, compress, maxWidth, quality);
        SysFile saved = fileService.upload(file, options);
        return ApiResponse.success(saved);
    }

    @Operation(summary = "获取文件元信息")
    @GetMapping("/{id}")
    @Parameter(name = "id", description = "文件ID")
    public ApiResponse<SysFile> get(@PathVariable("id") Long id) {
        return fileService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "文件不存在"));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    @Parameter(name = "id", description = "文件ID")
    public ResponseEntity<byte[]> download(@PathVariable("id") Long id) {
        FileStream stream = fileService.download(id);
        try (var is = stream.stream()) {
            byte[] content = is.readAllBytes();
            String filename = URLEncoder.encode(stream.fileName(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.parseMediaType(Optional.ofNullable(stream.mimeType()).orElse(MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)))
                    .contentLength(content.length)
                    .body(content);
        } catch (Exception e) {
            throw new BusinessException("下载失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    @Parameter(name = "id", description = "文件ID")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        fileService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    // ---------------- 分片上传 ----------------

    @Operation(summary = "初始化分片上传")
    @PostMapping("/multipart/initiate")
    @Parameter(name = "body", description = "初始化参数")
    public ApiResponse<MultipartSession> initiate(
            @RequestBody(description = "init request", content = @Content(schema = @Schema(implementation = MultipartInitiateRequestDTO.class)))
            @org.springframework.web.bind.annotation.RequestBody MultipartInitiateRequestDTO body) {
        MultipartSession session = fileService.initiateMultipart(new MultipartInitiateRequest(
                body.storageCode(), body.fileName(), body.contentType()
        ));
        return ApiResponse.success(session);
    }

    @Operation(summary = "上传分片")
    @PostMapping(value = "/multipart/upload-part", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameters(
            value = {
                    @Parameter(name = "storageCode", description = "存储编码"),
                    @Parameter(name = "objectKey", description = "对象Key"),
                    @Parameter(name = "uploadId", description = "上传ID"),
                    @Parameter(name = "partNumber", description = "分片编号"),
                    @Parameter(name = "file", description = "分片文件", required = true),
            }
    )
    public ApiResponse<MultipartPart> uploadPart(
            @RequestParam("storageCode") String storageCode,
            @RequestParam("objectKey") String objectKey,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("partNumber") Integer partNumber,
            @RequestPart("file") MultipartFile part
    ) {
        try (var is = part.getInputStream()) {
            MultipartPart res = fileService.uploadPart(new MultipartUploadPartRequest(
                    storageCode, objectKey, uploadId, partNumber, is, part.getSize()
            ));
            return ApiResponse.success(res);
        } catch (java.io.IOException io) {
            throw new cn.refinex.core.exception.BusinessException("读取分片数据失败: " + io.getMessage());
        }
    }

    @Operation(summary = "完成分片上传")
    @PostMapping("/multipart/complete")
    @Parameter(name = "body", description = "完成参数")
    public ApiResponse<SysFile> complete(@org.springframework.web.bind.annotation.RequestBody MultipartCompleteRequestDTO body) {
        SysFile file = fileService.completeMultipart(new MultipartCompleteRequest(
                body.storageCode(), body.objectKey(), body.uploadId(), body.etags(),
                body.fileName(), body.mimeType(), body.bizType(), body.bizId(), body.title()
        ));
        return ApiResponse.success(file);
    }

    @Operation(summary = "终止分片上传")
    @PostMapping("/multipart/abort")
    @Parameter(name = "body", description = "终止参数")
    public ApiResponse<Void> abort(@org.springframework.web.bind.annotation.RequestBody MultipartAbortRequestDTO body) {
        fileService.abortMultipart(new MultipartAbortRequest(body.storageCode(), body.objectKey(), body.uploadId()));
        return ApiResponse.success();
    }
}
