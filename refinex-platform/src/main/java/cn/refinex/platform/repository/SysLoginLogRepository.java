package cn.refinex.platform.repository;

import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.entity.SysLoginLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;

/**
 * 登录日志仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysLoginLogRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 插入登录日志
     *
     * @param log      登录日志
     * @param manager  数据库模板管理器
     */
    public void insert(SysLoginLog log, JdbcTemplateManager manager) {
        JdbcTemplateManager executor = Objects.requireNonNull(manager, "JdbcTemplateManager is required for write operations");
        String sql = """
                INSERT INTO sys_login_log (
                    user_id, username, login_identity, status, message,
                    login_ip, login_location, device_type, user_agent, login_time,
                    create_by, create_time, update_by, update_time)
                VALUES (
                    :userId, :username, :loginIdentity, :status, :message,
                    :loginIp, :loginLocation, :deviceType, :userAgent, :loginTime,
                    :createBy, :createTime, :updateBy, :updateTime)
                """;
        Map<String, Object> params = BeanUtils.beanToMap(log, false, false);
        executor.update(sql, params);
    }
}
