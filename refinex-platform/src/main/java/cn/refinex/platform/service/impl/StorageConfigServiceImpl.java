package cn.refinex.platform.service.impl;

import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.service.CryptoService;
import cn.refinex.core.service.CryptoService.EncryptedValue;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.controller.file.dto.request.StorageConfigCreateRequestDTO;
import cn.refinex.platform.controller.file.dto.request.StorageConfigUpdateRequestDTO;
import cn.refinex.platform.controller.file.dto.response.StorageConfigResponseDTO;
import cn.refinex.platform.converter.StorageConfigConverter;
import cn.refinex.platform.entity.SysStorageConfig;
import cn.refinex.platform.infra.file.storage.impl.S3FileStorageClient;
import cn.refinex.platform.repository.SysStorageConfigRepository;
import cn.refinex.platform.service.StorageConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 存储配置服务实现（带简单缓存）
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class StorageConfigServiceImpl implements StorageConfigService {

    private final SysStorageConfigRepository repository;
    private final JdbcTemplateManager jdbcManager;
    private final CryptoService cryptoService;
    private final StorageConfigConverter converter;

    /**
     * 根据编码获取配置，若 code 为空则返回默认配置。
     *
     * @param storageCode 存储配置编码
     * @return 存储配置配置
     */
    @Override
    @Cacheable(cacheNames = "storageConfigEntityByCode", key = "#storageCode == null || #storageCode.isBlank() ? '__default__' : #storageCode", unless = "#result == null || #result.isEmpty()")
    public Optional<SysStorageConfig> getByCodeOrDefault(String storageCode) {
        if (storageCode == null || storageCode.isBlank()) {
            return repository.findDefaultActive();
        }
        return repository.findByCode(storageCode);
    }

    /**
     * 列表查询存储配置
     *
     * @return 存储配置列表
     */
    @Override
    @Cacheable(cacheNames = "storageConfigList", key = "'all'")
    public List<StorageConfigResponseDTO> list() {
        return repository.listAll().stream().map(converter::toResponse).toList();
    }

    /**
     * 根据编码获取存储配置
     *
     * @param code 存储配置编码
     * @return 存储配置
     */
    @Override
    @Cacheable(cacheNames = "storageConfigDtoByCode", key = "#code", unless = "#result == null || #result.isEmpty()")
    public Optional<StorageConfigResponseDTO> getByCode(String code) {
        return repository.findByCode(code).map(converter::toResponse);
    }

    /**
     * 创建存储配置
     *
     * @param req 创建请求
     * @param operatorId 操作人 ID
     */
    @Override
    @CacheEvict(cacheNames = {"storageConfigList", "storageConfigDtoByCode", "storageConfigEntityByCode"}, allEntries = true)
    public void create(StorageConfigCreateRequestDTO req, Long operatorId) {
        if (repository.findByCode(req.storageCode()).isPresent()) {
            throw new BusinessException("存储编码已存在: " + req.storageCode());
        }
        SysStorageConfig cfg = buildCreateEntity(req, operatorId);
        applyEncryptedSecretsForCreate(cfg, req);

        jdbcManager.executeInTransaction(jdbc -> {
            long newId = repository.insertAndReturnId(cfg, jdbc);
            cfg.setId(newId);
            if (cfg.getIsDefault() != null && cfg.getIsDefault() == 1) {
                repository.clearDefaultExcept(cfg.getStorageCode(), jdbc);
            }
            return null;
        });
    }

    /**
     * 更新存储配置
     *
     * @param code 编码
     * @param req  请求参数
     */
    @Override
    @CacheEvict(cacheNames = {"storageConfigList", "storageConfigDtoByCode", "storageConfigEntityByCode"}, allEntries = true)
    public void update(String code, StorageConfigUpdateRequestDTO req, Long operatorId) {
        SysStorageConfig exist = repository.findByCode(code).orElseThrow(() -> new BusinessException("存储配置不存在: " + code));
        SysStorageConfig cfg = buildUpdateEntity(exist, req, operatorId, code);
        applyEncryptedSecretsForUpdate(cfg, req, exist);

        jdbcManager.executeInTransaction(jdbc -> {
            repository.updateByCode(cfg, jdbc);
            if (cfg.getIsDefault() != null && cfg.getIsDefault() == 1) {
                repository.clearDefaultExcept(cfg.getStorageCode(), jdbc);
            }
            return null;
        });
        // 失效对应 S3 客户端（如配置影响 S3）
        S3FileStorageClient.invalidate(exist);
    }

    /**
     * 删除存储配置
     *
     * @param code       编码
     * @param operatorId 操作人ID
     */
    @Override
    @CacheEvict(cacheNames = {"storageConfigList", "storageConfigDtoByCode", "storageConfigEntityByCode"}, allEntries = true)
    public void delete(String code, Long operatorId) {
        Optional<SysStorageConfig> existOpt = repository.findByCode(code);
        if (existOpt.isEmpty()) {
            throw new BusinessException("存储配置不存在: " + code);
        }
        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDeleteByCode(code, operatorId, jdbc);
            return null;
        });
        // 失效 S3 客户端
        existOpt.ifPresent(S3FileStorageClient::invalidate);
    }

    // -------------------- Helpers --------------------

    /**
     * 创建存储配置
     *
     * @param req 创建请求
     * @param operatorId 操作人ID
     * @return 存储配置
     */
    private SysStorageConfig buildCreateEntity(StorageConfigCreateRequestDTO req, Long operatorId) {
        SysStorageConfig cfg = new SysStorageConfig();
        cfg.setStorageCode(req.storageCode());
        cfg.setStorageName(req.storageName());
        cfg.setStorageType(req.storageType());
        cfg.setEndpoint(req.endpoint());
        cfg.setRegion(req.region());
        cfg.setBucket(req.bucket());
        cfg.setBasePath(req.basePath());
        cfg.setBaseUrl(req.baseUrl());
        cfg.setSessionPolicy(req.sessionPolicy());
        cfg.setIsDefault(req.isDefault() != null && req.isDefault() == 1 ? 1 : 0);
        cfg.setExtConfig(req.extConfig());
        cfg.setStatus(req.status() == null ? 1 : req.status());
        cfg.setRemark(req.remark());
        cfg.setCreateBy(operatorId);
        cfg.setCreateTime(LocalDateTime.now());
        cfg.setUpdateBy(operatorId);
        cfg.setUpdateTime(LocalDateTime.now());
        cfg.setDeleted(0);
        return cfg;
    }

    /**
     * 更新存储配置
     *
     * @param exist 存储配置
     * @param req   更新请求
     * @param operatorId 操作人ID
     * @param code 存储配置编码
     * @return 存储配置
     */
    private SysStorageConfig buildUpdateEntity(SysStorageConfig exist, StorageConfigUpdateRequestDTO req, Long operatorId, String code) {
        SysStorageConfig cfg = new SysStorageConfig();
        cfg.setStorageCode(code);
        cfg.setStorageName(Optional.ofNullable(req.storageName()).orElse(exist.getStorageName()));
        cfg.setStorageType(Optional.ofNullable(req.storageType()).orElse(exist.getStorageType()));
        cfg.setEndpoint(Optional.ofNullable(req.endpoint()).orElse(exist.getEndpoint()));
        cfg.setRegion(Optional.ofNullable(req.region()).orElse(exist.getRegion()));
        cfg.setBucket(Optional.ofNullable(req.bucket()).orElse(exist.getBucket()));
        cfg.setBasePath(Optional.ofNullable(req.basePath()).orElse(exist.getBasePath()));
        cfg.setBaseUrl(Optional.ofNullable(req.baseUrl()).orElse(exist.getBaseUrl()));
        cfg.setSessionPolicy(Optional.ofNullable(req.sessionPolicy()).orElse(exist.getSessionPolicy()));
        cfg.setIsDefault(req.isDefault() == 1 ? 1 : 0);
        cfg.setExtConfig(Optional.ofNullable(req.extConfig()).orElse(exist.getExtConfig()));
        cfg.setStatus(Optional.ofNullable(req.status()).orElse(exist.getStatus()));
        cfg.setRemark(Optional.ofNullable(req.remark()).orElse(exist.getRemark()));
        cfg.setUpdateBy(operatorId);
        cfg.setUpdateTime(LocalDateTime.now());
        return cfg;
    }

    /**
     * 应用加密密钥
     *
     * @param cfg 存储配置
     * @param req 请求参数
     */
    private void applyEncryptedSecretsForCreate(SysStorageConfig cfg, StorageConfigCreateRequestDTO req) {
        try {
            if (req.accessKeyPlain() != null && !req.accessKeyPlain().isBlank()) {
                EncryptedValue ev = cryptoService.encrypt(req.accessKeyPlain());
                cfg.setAccessKeyCipher(ev.cipher());
                cfg.setAccessKeyIndex(ev.index());
            }
            if (req.secretKeyPlain() != null && !req.secretKeyPlain().isBlank()) {
                EncryptedValue ev = cryptoService.encrypt(req.secretKeyPlain());
                cfg.setSecretKeyCipher(ev.cipher());
                cfg.setSecretKeyIndex(ev.index());
            }
        } catch (Exception e) {
            throw new BusinessException("加密存储密钥失败: " + e.getMessage());
        }
    }

    /**
     * 应用加密密钥
     *
     * @param cfg 存储配置
     * @param req 请求参数
     * @param exist 存储配置
     */
    private void applyEncryptedSecretsForUpdate(SysStorageConfig cfg, StorageConfigUpdateRequestDTO req, SysStorageConfig exist) {
        try {
            if (req.accessKeyPlain() != null) {
                if (!req.accessKeyPlain().isBlank()) {
                    EncryptedValue ev = cryptoService.encrypt(req.accessKeyPlain());
                    cfg.setAccessKeyCipher(ev.cipher());
                    cfg.setAccessKeyIndex(ev.index());
                } else {
                    cfg.setAccessKeyCipher(null);
                    cfg.setAccessKeyIndex(null);
                }
            } else {
                cfg.setAccessKeyCipher(exist.getAccessKeyCipher());
                cfg.setAccessKeyIndex(exist.getAccessKeyIndex());
            }

            if (req.secretKeyPlain() != null) {
                if (!req.secretKeyPlain().isBlank()) {
                    EncryptedValue ev = cryptoService.encrypt(req.secretKeyPlain());
                    cfg.setSecretKeyCipher(ev.cipher());
                    cfg.setSecretKeyIndex(ev.index());
                } else {
                    cfg.setSecretKeyCipher(null);
                    cfg.setSecretKeyIndex(null);
                }
            } else {
                cfg.setSecretKeyCipher(exist.getSecretKeyCipher());
                cfg.setSecretKeyIndex(exist.getSecretKeyIndex());
            }
        } catch (Exception e) {
            throw new BusinessException("加密存储密钥失败: " + e.getMessage());
        }
    }
}
