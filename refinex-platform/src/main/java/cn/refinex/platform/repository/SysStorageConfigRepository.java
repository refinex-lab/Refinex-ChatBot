package cn.refinex.platform.repository;

import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.entity.SysStorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 存储配置仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysStorageConfigRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增存储配置
     *
     * @param cfg    存储配置
     * @param jdbcTx 事务管理器
     */
    public void insert(SysStorageConfig cfg, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO sys_storage_config (
                  storage_code, storage_name, storage_type, endpoint, region, bucket, base_path, base_url,
                  access_key_cipher, access_key_index, secret_key_cipher, secret_key_index,
                  session_policy, is_default, ext_config, status, create_by, create_time, update_by, update_time, deleted, remark
                ) VALUES (
                  :storageCode, :storageName, :storageType, :endpoint, :region, :bucket, :basePath, :baseUrl,
                  :accessKeyCipher, :accessKeyIndex, :secretKeyCipher, :secretKeyIndex,
                  :sessionPolicy, :isDefault, :extConfig, :status, :createBy, :createTime, :updateBy, :updateTime, :deleted, :remark
                )
                """;
        Map<String, Object> params = BeanUtils.beanToMap(cfg, false, false);
        jdbcTx.update(sql, params);
    }

    /**
     * 新增并返回主键
     */
    public long insertAndReturnId(SysStorageConfig cfg, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO sys_storage_config (
                  storage_code, storage_name, storage_type, endpoint, region, bucket, base_path, base_url,
                  access_key_cipher, access_key_index, secret_key_cipher, secret_key_index,
                  session_policy, is_default, ext_config, status, create_by, create_time, update_by, update_time, deleted, remark
                ) VALUES (
                  :storageCode, :storageName, :storageType, :endpoint, :region, :bucket, :basePath, :baseUrl,
                  :accessKeyCipher, :accessKeyIndex, :secretKeyCipher, :secretKeyIndex,
                  :sessionPolicy, :isDefault, :extConfig, :status, :createBy, :createTime, :updateBy, :updateTime, :deleted, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(cfg, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新存储配置（按 storage_code）
     *
     * @param cfg    存储配置
     * @param jdbcTx 事务管理器
     */
    public void updateByCode(SysStorageConfig cfg, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE sys_storage_config
                SET storage_name = :storageName,
                    storage_type = :storageType,
                    endpoint = :endpoint,
                    region = :region,
                    bucket = :bucket,
                    base_path = :basePath,
                    base_url = :baseUrl,
                    access_key_cipher = :accessKeyCipher,
                    access_key_index = :accessKeyIndex,
                    secret_key_cipher = :secretKeyCipher,
                    secret_key_index = :secretKeyIndex,
                    session_policy = :sessionPolicy,
                    is_default = :isDefault,
                    ext_config = :extConfig,
                    status = :status,
                    update_by = :updateBy,
                    update_time = :updateTime
                WHERE storage_code = :storageCode
                  AND deleted = 0
                """;
        Map<String, Object> params = BeanUtils.beanToMap(cfg, false, false);
        jdbcTx.update(sql, params);
    }

    /**
     * 逻辑删除（按 code）
     *
     * @param code     存储编码
     * @param operator 操作人ID
     * @param jdbcTx   事务管理器
     */
    public void logicalDeleteByCode(String code, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE sys_storage_config
                SET deleted = 1, update_by = :operator, update_time = NOW()
                WHERE storage_code = :code
                """;
        jdbcTx.update(sql, Map.of("operator", operator, "code", code));
    }

    /**
     * 根据存储编码查询
     *
     * @param storageCode 存储编码
     * @return 存储配置
     */
    public Optional<SysStorageConfig> findByCode(String storageCode) {
        String sql = """
                SELECT *
                FROM sys_storage_config
                WHERE storage_code = :code
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", storageCode), SysStorageConfig.class));
    }

    /**
     * 查询默认启用的存储配置
     *
     * @return 默认存储配置
     */
    public Optional<SysStorageConfig> findDefaultActive() {
        String sql = """
                SELECT *
                FROM sys_storage_config
                WHERE is_default = 1
                  AND status = 1
                  AND deleted = 0
                LIMIT 1
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of(), SysStorageConfig.class));
    }

    /**
     * 清除其它默认
     *
     * @param code   存储编码
     * @param jdbcTx 事务管理器
     */
    public void clearDefaultExcept(String code, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE sys_storage_config
                SET is_default = 0
                WHERE is_default = 1
                  AND storage_code <> :code
                  AND deleted = 0
                """;
        jdbcTx.update(sql, Map.of("code", code));
    }

    /**
     * 列表
     *
     * @return 列表
     */
    public List<SysStorageConfig> listAll() {
        String sql = """
                SELECT *
                FROM sys_storage_config
                WHERE deleted = 0
                ORDER BY is_default DESC, update_time DESC
                """;
        return jdbcManager.queryList(sql, java.util.Map.of(), SysStorageConfig.class);
    }
}
