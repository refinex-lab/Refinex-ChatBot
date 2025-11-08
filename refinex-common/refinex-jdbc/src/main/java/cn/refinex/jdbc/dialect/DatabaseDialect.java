package cn.refinex.jdbc.dialect;

/**
 * 数据库方言接口
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface DatabaseDialect {

    /**
     * 获取分页 SQL
     *
     * @param sql    原始 SQL
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 分页 SQL
     */
    String getLimitSql(String sql, long offset, long limit);

    /**
     * 是否支持自动生成主键
     *
     * @return 是否支持
     */
    boolean supportsGeneratedKeys();

    /**
     * 获取序列下一个值的 SQL
     *
     * @param sequenceName 序列名称
     * @return SQL 语句
     */
    String getSequenceNextValSql(String sequenceName);
}
