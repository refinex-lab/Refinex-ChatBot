package cn.refinex.platform.repository;

import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.entity.SysFile;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 系统文件仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysFileRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 保存文件元信息
     *
     * @param file   文件元信息
     * @param jdbcTx 事务管理器（必须）
     */
    public void insert(SysFile file, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO sys_file (
                  storage_code, file_key, uri, file_name, ext, mime_type, size_bytes,
                  checksum_sha256, width, height, duration_ms, encrypt_algo, is_db_stored,
                  biz_type, biz_id, title, sort, status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :storageCode, :fileKey, :uri, :fileName, :ext, :mimeType, :sizeBytes,
                  :checksumSha256, :width, :height, :durationMs, :encryptAlgo, :isDbStored,
                  :bizType, :bizId, :title, :sort, :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        Map<String, Object> params = BeanUtils.beanToMap(file, false, false);
        jdbcTx.update(sql, params);
    }

    /**
     * 新增并返回主键
     *
     * @param file   文件元信息
     * @param jdbcTx 事务管理器
     * @return 新增主键ID
     */
    public long insertAndReturnId(SysFile file, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO sys_file (
                  storage_code, file_key, uri, file_name, ext, mime_type, size_bytes,
                  checksum_sha256, width, height, duration_ms, encrypt_algo, is_db_stored,
                  biz_type, biz_id, title, sort, status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :storageCode, :fileKey, :uri, :fileName, :ext, :mimeType, :sizeBytes,
                  :checksumSha256, :width, :height, :durationMs, :encryptAlgo, :isDbStored,
                  :bizType, :bizId, :title, :sort, :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(file, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 根据ID查询
     *
     * @param id 文件ID
     * @return 文件元信息
     */
    public Optional<SysFile> findById(Long id) {
        String sql = """
                SELECT *
                FROM sys_file
                WHERE id = :id AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id), SysFile.class));
    }

    /**
     * 逻辑删除
     *
     * @param id       文件ID
     * @param deleteBy 删除人ID
     * @param jdbcTx   事务管理器（必须）
     */
    public void logicalDelete(Long id, Long deleteBy, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE sys_file
                SET deleted = 1,
                    delete_by = :deleteBy,
                    delete_time = :deleteTime
                WHERE id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("deleteBy", deleteBy);
        params.put("deleteTime", LocalDateTime.now());
        jdbcTx.update(sql, params);
    }

    /**
     * 更新文件URI
     *
     * @param id     文件ID
     * @param uri    文件URI
     * @param jdbcTx 事务管理器（必须）
     */
    public void updateUri(Long id, String uri, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE sys_file
                SET uri = :uri,
                    update_time = :updateTime
                WHERE id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("uri", uri);
        params.put("updateTime", LocalDateTime.now());
        jdbcTx.update(sql, params);
    }

    /**
     * 更新文件Key与URI
     *
     * @param id     文件ID
     * @param fileKey 对象Key
     * @param uri    访问URI
     * @param jdbcTx 事务管理器
     */
    public void updateFileKeyAndUri(Long id, String fileKey, String uri, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE sys_file
                SET file_key = :fileKey,
                    uri = :uri,
                    update_time = :updateTime
                WHERE id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("fileKey", fileKey);
        params.put("uri", uri);
        params.put("updateTime", LocalDateTime.now());
        jdbcTx.update(sql, params);
    }
}
