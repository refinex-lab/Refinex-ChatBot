package cn.refinex.platform.service;

import cn.refinex.platform.controller.file.dto.request.StorageConfigCreateRequestDTO;
import cn.refinex.platform.controller.file.dto.request.StorageConfigUpdateRequestDTO;
import cn.refinex.platform.controller.file.dto.response.StorageConfigResponseDTO;
import cn.refinex.platform.entity.SysStorageConfig;

import java.util.List;
import java.util.Optional;

/**
 * 存储配置服务，封装读取/缓存等。
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface StorageConfigService {

    /**
     * 根据编码获取配置，若 code 为空则返回默认配置。
     *
     * @param storageCode 存储配置编码
     * @return 存储配置配置
     */
    Optional<SysStorageConfig> getByCodeOrDefault(String storageCode);

    /**
     * 列表查询存储配置
     *
     * @return 存储配置列表
     */
    List<StorageConfigResponseDTO> list();

    /**
     * 根据编码查询（管理视图）
     *
     * @param code 编码
     * @return 存储配置
     */
    Optional<StorageConfigResponseDTO> getByCode(String code);

    /**
     * 新增存储配置
     *
     * @param req        请求参数
     * @param operatorId 操作人ID
     */
    void create(StorageConfigCreateRequestDTO req, Long operatorId);

    /**
     * 更新存储配置
     *
     * @param code 编码
     * @param req  请求参数
     */
    void update(String code, StorageConfigUpdateRequestDTO req, Long operatorId);

    /**
     * 删除存储配置
     *
     * @param code       编码
     * @param operatorId 操作人ID
     */
    void delete(String code, Long operatorId);
}
