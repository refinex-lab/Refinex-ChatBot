package cn.refinex.platform.repository;

import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 权限仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysPermissionRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 根据用户 ID 查询权限列表
     *
     * @param userId 用户 ID
     * @return 权限列表
     */
    public List<String> listPermissionsByUserId(Long userId) {
        String sql = """
                SELECT DISTINCT mo.permission
                FROM sys_menu_op mo
                INNER JOIN sys_menu_role_op mro ON mo.id = mro.menu_op_id
                INNER JOIN sys_user_role ur ON mro.role_id = ur.role_id
                WHERE ur.user_id = :userId
                  AND mo.permission IS NOT NULL
                  AND mo.permission <> ''
                  AND mo.deleted = 0
                  AND mo.status = 1
                """;
        return jdbcManager.queryColumn(sql, Map.of("userId", userId), String.class);
    }
}
