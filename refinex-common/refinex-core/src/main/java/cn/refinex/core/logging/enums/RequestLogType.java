package cn.refinex.core.logging.enums;

/**
 * 请求日志业务类型
 *
 * @author Refinex
 * @since 1.0.0
 */
public enum RequestLogType {

    /**
     * 创建
     */
    CREATE,

    /**
     * 更新
     */
    UPDATE,

    /**
     * 删除
     */
    DELETE,

    /**
     * 查询
     */
    QUERY,

    /**
     * 登录
     */
    LOGIN,

    /**
     * 登出
     */
    LOGOUT,

    /**
     * 导入
     */
    IMPORT,

    /**
     * 导出
     */
    EXPORT,

    /**
     * 其他
     */
    OTHER
}
