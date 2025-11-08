package cn.refinex.jdbc.dialect;

/**
 * MySQL 数据库方言
 *
 * @author Refinex
 * @since 1.0.0
 */
public class MySQLDialect implements DatabaseDialect {

    /**
     * 获取分页 SQL
     *
     * <p>
     * MySQL 分页 SQL 格式:
     * <pre>{@code
     * SELECT * FROM table_name LIMIT offset, limit;
     * }</pre>
     *
     * @param sql    原始 SQL
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 分页 SQL
     */
    @Override
    public String getLimitSql(String sql, long offset, long limit) {
        return sql + " LIMIT " + offset + ", " + limit;
    }

    /**
     * 是否支持自动生成主键
     * <p>
     * MySQL 支持自动生成主键:
     * 1. 表定义时, 主键字段添加 {@code AUTO_INCREMENT} 属性
     * 2. 插入语句中, 不指定主键值, 数据库会自动生成主键
     *
     * @return 是否支持
     */
    @Override
    public boolean supportsGeneratedKeys() {
        return true;
    }

    /**
     * 获取序列下一个值的 SQL
     *
     * @param sequenceName 序列名称
     * @return SQL 语句
     */
    @Override
    public String getSequenceNextValSql(String sequenceName) {
        throw new UnsupportedOperationException("MySQL 不支持序列");
    }
}
