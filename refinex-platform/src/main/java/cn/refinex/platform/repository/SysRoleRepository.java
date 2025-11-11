package cn.refinex.platform.repository;

import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 角色仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysRoleRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 根据用户 ID 查询角色编码列表
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    public List<String> listRoleCodesByUserId(Long userId) {
        String sql = """
                SELECT DISTINCT r.role_code
                FROM sys_role r
                INNER JOIN sys_user_role ur ON r.id = ur.role_id
                WHERE ur.user_id = :userId
                  AND r.deleted = 0
                  AND r.status = 1
                """;
        return jdbcManager.queryColumn(sql, Map.of("userId", userId), String.class);
    }

    /**
     * 根据角色编码查询角色 ID
     *
     * @param roleCode 角色编码
     * @return 角色 ID
     */
    public Optional<Long> findRoleIdByCode(String roleCode) {
        String sql = """
                SELECT id
                FROM sys_role
                WHERE role_code = :roleCode
                  AND deleted = 0
                LIMIT 1
                """;
        return Optional.ofNullable(jdbcManager.queryLong(sql, Map.of("roleCode", roleCode)));
    }
}
