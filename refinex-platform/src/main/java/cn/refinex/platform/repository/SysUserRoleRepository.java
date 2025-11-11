package cn.refinex.platform.repository;

import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 用户角色关联仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysUserRoleRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 插入用户角色关联
     *
     * @param userId  用户 ID
     * @param roleId  角色 ID
     * @param manager 数据库模板管理器
     */
    public void insert(Long userId, Long roleId, JdbcTemplateManager manager) {
        JdbcTemplateManager executor = Objects.requireNonNull(manager, "JdbcTemplateManager is required for write operations");
        String sql = """
                INSERT INTO sys_user_role (user_id, role_id, create_by, create_time)
                VALUES (:userId, :roleId, :createBy, :createTime)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("roleId", roleId);
        params.put("createBy", userId);
        params.put("createTime", LocalDateTime.now());
        executor.update(sql, params);
    }
}
