package cn.refinex.platform.repository;

import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.entity.SysFileData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

/**
 * 系统文件数据仓储（仅 DB 存储场景）
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysFileDataRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 插入文件数据
     *
     * @param data   文件数据
     * @param jdbcTx 事务管理器（必须）
     */
    public void insert(SysFileData data, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO sys_file_data (file_id, data, create_by, create_time, update_by, update_time, deleted)
                VALUES (:fileId, :data, :createBy, :createTime, :updateBy, :updateTime, :deleted)
                """;
        Map<String, Object> params = BeanUtils.beanToMap(data, false, false);
        jdbcTx.update(sql, params);
    }

    /**
     * 根据文件ID查询二进制
     *
     * @param fileId 文件ID
     * @return 文件数据
     */
    public Optional<SysFileData> findByFileId(Long fileId) {
        String sql = """
                SELECT *
                FROM sys_file_data
                WHERE file_id = :fileId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("fileId", fileId), SysFileData.class));
    }

    /**
     * 逻辑删除文件数据
     *
     * @param fileId 文件ID
     * @param jdbcTx 事务管理器
     */
    public void logicalDeleteByFileId(Long fileId, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE sys_file_data
                SET deleted = 1
                WHERE file_id = :fileId
                """;
        jdbcTx.update(sql, Map.of("fileId", fileId));
    }
}
