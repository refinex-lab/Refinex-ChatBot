package cn.refinex.jdbc.dialect;

/**
 * PostgreSQL 数据库方言
 *
 * @author Refinex
 * @since 1.0.0
 */
public class PostgreSQLDialect implements DatabaseDialect {

    /**
     * 获取分页 SQL
     * <p>
     * PostgreSQL 分页 SQL 格式:
     * <pre>{@code
     * SELECT * FROM (original_sql) tmp_page LIMIT limit OFFSET offset;
     * }</pre>
     *
     * @param sql    原始 SQL
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 分页 SQL
     */
    @Override
    public String getLimitSql(String sql, long offset, long limit) {
        return sql + " LIMIT " + limit + " OFFSET " + offset;
    }

    /**
     * 是否支持自动生成主键
     * <p>
     * PostgreSQL 支持自动生成主键:
     * 1. 表定义时, 主键字段添加 {@code SERIAL} 属性
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
     * <p>
     * PostgreSQL 序列 SQL 格式:
     * <pre>{@code
     * SELECT NEXTVAL('sequence_name');
     * }</pre>
     *
     * @param sequenceName 序列名称
     * @return SQL 语句
     */
    @Override
    public String getSequenceNextValSql(String sequenceName) {
        return "SELECT NEXTVAL('" + sequenceName + "')";
    }
}
