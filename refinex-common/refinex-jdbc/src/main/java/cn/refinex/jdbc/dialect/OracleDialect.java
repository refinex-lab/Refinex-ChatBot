package cn.refinex.jdbc.dialect;

/**
 * Oracle 数据库方言
 *
 * @author Refinex
 * @since 1.0.0
 */
public class OracleDialect implements DatabaseDialect {

    /**
     * 获取分页 SQL
     *
     * <p>
     * Oracle 分页 SQL 格式:
     * <pre>{@code
     * SELECT * FROM (SELECT tmp_page.*, ROWNUM row_id FROM (
     *     original_sql
     * ) tmp_page WHERE ROWNUM <= end_row) WHERE row_id >= start_row;
     * }</pre>
     *
     * @param sql    原始 SQL
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 分页 SQL
     */
    @Override
    public String getLimitSql(String sql, long offset, long limit) {
        // Oracle 分页从 1 开始
        long startRow = offset + 1;
        // Oracle 分页结束行 = 偏移量 + 限制数量
        long endRow = offset + limit;

        return "SELECT * FROM (SELECT tmp_page.*, ROWNUM row_id FROM (" +
                sql +
                ") tmp_page WHERE ROWNUM <= " + endRow +
                ") WHERE row_id >= " + startRow;
    }

    /**
     * 是否支持自动生成主键
     *
     * @return 是否支持
     */
    @Override
    public boolean supportsGeneratedKeys() {
        // Oracle 不支持自动生成主键
        return false;
    }

    /**
     * 获取序列下一个值的 SQL
     * <p>
     * Oracle 序列 SQL 格式:
     * <pre>{@code
     * SELECT sequence_name.NEXTVAL FROM DUAL;
     * }</pre>
     *
     * @param sequenceName 序列名称
     * @return SQL 语句
     */
    @Override
    public String getSequenceNextValSql(String sequenceName) {
        return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
    }
}
