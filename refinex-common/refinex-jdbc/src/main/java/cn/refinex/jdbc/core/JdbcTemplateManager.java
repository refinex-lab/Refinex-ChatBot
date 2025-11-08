package cn.refinex.jdbc.core;

import cn.hutool.core.util.StrUtil;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.domain.PageQuery;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.exception.SystemException;
import cn.refinex.jdbc.callback.InputStreamCallback;
import cn.refinex.jdbc.callback.TransactionCallback;
import cn.refinex.jdbc.dialect.DatabaseDialect;
import cn.refinex.jdbc.dialect.MySQLDialect;
import cn.refinex.jdbc.enums.LogFormatType;
import cn.refinex.core.domain.SortOrder;
import cn.refinex.jdbc.masker.SensitiveDataMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.*;

/**
 * JDBC 模板管理器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
public class JdbcTemplateManager {

    /**
     * 命名参数 JDBC 模板
     */
    @Getter
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 数据源事务管理器
     */
    private final DataSourceTransactionManager dataSourceTransactionManager;

    /**
     * 事务定义
     */
    private final TransactionDefinition transactionDefinition;

    /**
     * 敏感数据脱敏器
     */
    @Setter
    private SensitiveDataMasker sensitiveDataMasker;

    /**
     * 数据库方言
     */
    @Setter
    private DatabaseDialect databaseDialect;

    /**
     * 是否启用列名转小写（默认启用）
     */
    @Setter
    private boolean lowerCaseColumnNames = true;

    /**
     * 慢查询阈值（毫秒）
     */
    @Setter
    private long slowQueryThresholdMs = 1000;

    /**
     * 是否启用列名冲突检测
     */
    @Setter
    private boolean enableColumnConflictCheck = true;

    /**
     * 日志格式（text 或 json）
     */
    @Setter
    private String logFormat = LogFormatType.TEXT.getValue();

    /**
     * JSON 对象映射器
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ========================= 构造函数 =========================

    /**
     * 构造函数，创建非事务 JDBC 模板管理器
     *
     * @param namedParameterJdbcTemplate 命名参数 JDBC 模板
     */
    public JdbcTemplateManager(@NonNull NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.dataSourceTransactionManager = null;
        this.transactionDefinition = null;
        this.databaseDialect = new MySQLDialect();
    }

    /**
     * 构造函数，创建事务 JDBC 模板管理器
     *
     * @param namedParameterJdbcTemplate   命名参数 JDBC 模板
     * @param dataSourceTransactionManager 数据源事务管理器
     * @param transactionDefinition        事务定义
     */
    public JdbcTemplateManager(@NonNull NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                               @NonNull DataSourceTransactionManager dataSourceTransactionManager,
                               @NonNull TransactionDefinition transactionDefinition) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.dataSourceTransactionManager = dataSourceTransactionManager;
        this.transactionDefinition = transactionDefinition;
        this.databaseDialect = new MySQLDialect();
    }

    // ========================= 查询方法 =========================

    /**
     * 查询整数
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 整数，查询无结果时返回 null
     */
    public Integer queryInt(String sql, Map<String, Object> params) {
        return this.queryInt(sql, params, false);
    }

    /**
     * 查询整数
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 整数，查询无结果时返回 null
     */
    public Integer queryInt(String sql, Map<String, Object> params, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Integer result = null;
        int rowCount = 0;
        Exception exception = null;

        try {
            result = this.namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
            rowCount = 1;
        } catch (EmptyResultDataAccessException e) {
            // 查询无结果，返回 null
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryInt", sql, params, elapsedMs, null, rowCount, exception);
            }
        }

        return result;
    }

    /**
     * 查询长整数
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 长整数，查询无结果时返回 null
     */
    public Long queryLong(String sql, Map<String, Object> params) {
        return this.queryLong(sql, params, false);
    }

    /**
     * 查询长整数
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 长整数，查询无结果时返回 null
     */
    public Long queryLong(String sql, Map<String, Object> params, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Long result = null;
        int rowCount = 0;
        Exception exception = null;

        try {
            result = this.namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
            rowCount = 1;
        } catch (EmptyResultDataAccessException e) {
            // 查询无结果，返回 null
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryLong", sql, params, elapsedMs, null, rowCount, exception);
            }
        }

        return result;
    }

    /**
     * 查询字符串
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 字符串，查询无结果时返回 null
     */
    public String queryString(String sql, Map<String, Object> params, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        String result = null;
        int rowCount = 0;
        Exception exception = null;

        try {
            result = this.namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
            rowCount = 1;
        } catch (EmptyResultDataAccessException e) {
            // 查询无结果，返回 null
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryString", sql, params, elapsedMs, null, rowCount, exception);
            }
        }

        return result;
    }

    /**
     * 查询二进制大对象
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 二进制大对象，查询无结果时返回 null
     */
    public Blob queryBlob(String sql, Map<String, Object> params) {
        return this.queryBlob(sql, params, false);
    }

    /**
     * 查询二进制大对象
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 二进制大对象，查询无结果时返回 null
     */
    public Blob queryBlob(String sql, Map<String, Object> params, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Blob result = null;
        int rowCount = 0;
        Exception exception = null;

        try {
            result = this.namedParameterJdbcTemplate.queryForObject(sql, params, Blob.class);
            rowCount = 1;
        } catch (EmptyResultDataAccessException e) {
            // 查询无结果，返回 null
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryBlob", sql, params, elapsedMs, null, rowCount, exception);
            }
        }

        return result;
    }

    /**
     * 查询输入流并通过回调处理（推荐使用，自动管理资源）
     * <p>
     * 该方法会自动管理 InputStream 的生命周期，在回调执行完毕后自动关闭流。
     * 这是处理 InputStream 查询的推荐方式，可以避免资源泄漏问题。
     *
     * @param sql      SQL 语句
     * @param params   参数
     * @param callback 处理回调
     * @param <T>      返回类型
     * @return 回调处理结果
     */
    public <T> T queryInputStreamWithCallback(String sql, Map<String, Object> params, InputStreamCallback<T> callback) {
        return this.queryInputStreamWithCallback(sql, params, false, callback);
    }

    /**
     * 查询输入流并通过回调处理（推荐使用，自动管理资源）
     * <p>
     * 该方法会自动管理 InputStream 的生命周期，在回调执行完毕后自动关闭流。
     * 这是处理 InputStream 查询的推荐方式，可以避免资源泄漏问题。
     *
     * @param sql      SQL 语句
     * @param params   参数
     * @param logSql   是否记录 SQL 日志
     * @param callback 处理回调
     * @param <T>      返回类型
     * @return 回调处理结果
     */
    public <T> T queryInputStreamWithCallback(String sql, Map<String, Object> params, boolean logSql, InputStreamCallback<T> callback) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        if (callback == null) {
            throw new IllegalArgumentException("回调函数不能为空");
        }

        long start = System.nanoTime();
        Exception exception = null;

        try {
            T result = this.namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    try (InputStream inputStream = rs.getBinaryStream(1)) {
                        return processInputStream(inputStream, callback);
                    } catch (IOException ioEx) {
                        throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "关闭输入流失败", ioEx);
                    }
                }
                return null;
            });

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryInputStreamWithCallback", sql, params, elapsedMs, null, result != null ? 1 : 0, null);
            }

            return result;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.error("查询并处理 InputStream 失败，sql: {}, params: {}, elapsedMs: {}ms", sql, params, elapsedMs, e);
            if (logSql) {
                this.logSqlStructured("queryInputStreamWithCallback", sql, params, elapsedMs, null, 0, exception);
            }
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "查询并处理输入流失败");
        }
    }

    /**
     * 处理输入流并通过回调处理
     *
     * @param inputStream 输入流
     * @param callback    处理回调
     * @param <T>         返回类型
     * @return 回调处理结果
     */
    private <T> T processInputStream(InputStream inputStream, InputStreamCallback<T> callback) throws IOException {
        try {
            return callback.process(inputStream);
        } catch (Exception ex) {
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "回调处理输入流失败", ex);
        }
    }

    /**
     * 查询列表
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 列表
     */
    public List<Map<String, Object>> queryList(String sql, Map<String, Object> params) {
        return this.queryList(sql, params, false);
    }

    /**
     * 查询列表
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 列表
     */
    public List<Map<String, Object>> queryList(String sql, Map<String, Object> params, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Exception exception = null;

        try {
            List<Map<String, Object>> resultList = this.namedParameterJdbcTemplate.queryForList(sql, params);

            List<Map<String, Object>> transformed = lowerCaseColumnNames
                    ? transformColumnNamesToLowerCase(resultList, null)
                    : resultList;

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryList", sql, params, elapsedMs, null, transformed.size(), null);
            }

            return transformed;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("queryList", sql, params, elapsedMs, null, 0, exception);
            }
            throw e;
        }
    }

    /**
     * 查询列表
     *
     * @param sql       SQL 语句
     * @param params    参数
     * @param logSql    是否记录 SQL 日志
     * @param nullValue 空值替换值
     * @return 列表
     */
    public List<Map<String, Object>> queryList(String sql, Map<String, Object> params, boolean logSql, Object nullValue) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Exception exception = null;

        try {
            List<Map<String, Object>> resultList = this.namedParameterJdbcTemplate.queryForList(sql, params);

            List<Map<String, Object>> transformed = lowerCaseColumnNames
                    ? transformColumnNamesToLowerCase(resultList, nullValue)
                    : replaceNullValues(resultList, nullValue);

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryListWithNullValue", sql, params, elapsedMs, null, transformed.size(), null);
            }

            return transformed;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("queryListWithNullValue", sql, params, elapsedMs, null, 0, exception);
            }
            throw e;
        }
    }

    /**
     * 查询列表（实体映射）
     *
     * @param sql            SQL 语句
     * @param params         参数
     * @param rowMapperClass 行映射类
     * @return 列表
     */
    public <T> List<T> queryList(String sql, Map<String, Object> params, Class<T> rowMapperClass) {
        return this.queryList(sql, params, false, rowMapperClass);
    }

    /**
     * 查询列表（实体映射）
     *
     * @param sql            SQL 语句
     * @param params         参数
     * @param logSql         是否记录 SQL 日志
     * @param rowMapperClass 行映射类
     * @return 列表
     */
    public <T> List<T> queryList(String sql, Map<String, Object> params, boolean logSql, Class<T> rowMapperClass) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        if (rowMapperClass == null) {
            throw new IllegalArgumentException("行映射类不能为空");
        }

        long start = System.nanoTime();
        Exception exception = null;

        try {
            List<T> result = this.namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(rowMapperClass));

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryListTyped", sql, params, elapsedMs, null, result.size(), null);
            }

            return result;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("queryListTyped", sql, params, elapsedMs, null, 0, exception);
            }
            throw e;
        }
    }

    /**
     * 查询单列值列表
     *
     * @param sql        SQL 语句
     * @param params     参数
     * @param columnType 列类型
     * @param <T>        列值类型
     * @return 单列值列表
     */
    public <T> List<T> queryColumn(String sql, Map<String, Object> params, Class<T> columnType) {
        return this.queryColumn(sql, params, false, columnType);
    }

    /**
     * 查询单列值列表
     *
     * @param sql        SQL 语句
     * @param params     参数
     * @param logSql     是否记录 SQL 日志
     * @param columnType 列类型
     * @param <T>        列值类型
     * @return 单列值列表
     */
    public <T> List<T> queryColumn(String sql, Map<String, Object> params, boolean logSql, Class<T> columnType) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        if (columnType == null) {
            throw new IllegalArgumentException("列类型不能为空");
        }

        long start = System.nanoTime();
        Exception exception = null;

        try {
            List<T> result = this.namedParameterJdbcTemplate.queryForList(sql, params, columnType);
            if (CollectionUtils.isEmpty(result)) {
                return result;
            }

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryColumn", sql, params, elapsedMs, null, result.size(), null);
            }

            return result;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("queryColumn", sql, params, elapsedMs, null, 0, exception);
            }
            throw e;
        }
    }

    /**
     * 查询 Map
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return Map，查询无结果时返回 null
     */
    public Map<String, Object> queryMap(String sql, Map<String, Object> params) {
        return this.queryMap(sql, params, false);
    }

    /**
     * 查询 Map
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return Map，查询无结果时返回 null
     */
    public Map<String, Object> queryMap(String sql, Map<String, Object> params, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Map<String, Object> result = null;
        int rowCount = 0;
        Exception exception = null;

        try {
            result = this.namedParameterJdbcTemplate.queryForMap(sql, params);
            rowCount = 1;

            if (lowerCaseColumnNames) {
                result = transformMapKeysToLowerCase(result);
            }
        } catch (EmptyResultDataAccessException e) {
            // 没有查询到结果，result 保持为 null，rowCount 为 0
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryMap", sql, params, elapsedMs, null, rowCount, exception);
            }
        }

        return result;
    }

    /**
     * 查询对象
     *
     * @param sql            SQL 语句
     * @param params         参数
     * @param rowMapperClass 行映射类
     * @return 对象，查询无结果时返回 null
     */
    public <T> T queryObject(String sql, Map<String, Object> params, Class<T> rowMapperClass) {
        return this.queryObject(sql, params, false, rowMapperClass);
    }

    /**
     * 查询对象
     *
     * @param sql            SQL 语句
     * @param params         参数
     * @param logSql         是否记录 SQL 日志
     * @param rowMapperClass 行映射类
     * @return 对象，查询无结果时返回 null
     */
    public <T> T queryObject(String sql, Map<String, Object> params, boolean logSql, Class<T> rowMapperClass) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        if (rowMapperClass == null) {
            throw new IllegalArgumentException("行映射类不能为空");
        }

        long start = System.nanoTime();
        T result = null;
        int rowCount = 0;
        Exception exception = null;

        try {
            result = this.namedParameterJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(rowMapperClass));
            rowCount = 1;
        } catch (EmptyResultDataAccessException e) {
            // 查询无结果，返回 null
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryObject", sql, params, elapsedMs, null, rowCount, exception);
            }
        }

        return result;
    }

    // ========================= 分页查询方法 =========================

    /**
     * 分页查询（Map 结果）
     *
     * @param sql       SQL 语句（不含分页语句）
     * @param params    参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    public PageResponse<Map<String, Object>> queryPage(String sql, Map<String, Object> params, PageQuery pageQuery) {
        return this.queryPage(sql, params, pageQuery, false);
    }

    /**
     * 分页查询（Map 结果）
     *
     * @param sql       SQL 语句（不含分页语句）
     * @param params    参数
     * @param pageQuery 分页参数
     * @param logSql    是否记录 SQL 日志
     * @return 分页结果
     */
    public PageResponse<Map<String, Object>> queryPage(String sql, Map<String, Object> params, @NonNull PageQuery pageQuery, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);
        pageQuery.validate();

        long start = System.nanoTime();
        Exception exception = null;

        try {
            // 查询总记录数
            String countSql = buildCountSql(sql);
            Long total = this.queryLong(countSql, params, logSql);
            if (total == null || total == 0) {
                return PageResponse.empty(pageQuery.getCurrent(), pageQuery.getSize());
            }

            // 构建分页 SQL
            String pageSql = buildPageSql(sql, pageQuery);

            // 查询分页数据
            List<Map<String, Object>> records = this.queryList(pageSql, params, logSql);

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryPage", sql, params, elapsedMs, null, records.size(), null);
            }

            return PageResponse.of(records, pageQuery, total);
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("queryPage", sql, params, elapsedMs, null, 0, exception);
            }
            throw e;
        }
    }

    /**
     * 分页查询（实体映射）
     *
     * @param sql            SQL 语句（不含分页语句）
     * @param params         参数
     * @param pageQuery      分页参数
     * @param rowMapperClass 行映射类
     * @param <T>            实体类型
     * @return 分页结果
     */
    public <T> PageResponse<T> queryPage(String sql, Map<String, Object> params, PageQuery pageQuery, Class<T> rowMapperClass) {
        return this.queryPage(sql, params, pageQuery, false, rowMapperClass);
    }

    /**
     * 分页查询（实体映射）
     *
     * @param sql            SQL 语句（不含分页语句）
     * @param params         参数
     * @param pageQuery      分页参数
     * @param logSql         是否记录 SQL 日志
     * @param rowMapperClass 行映射类
     * @param <T>            实体类型
     * @return 分页结果
     */
    public <T> PageResponse<T> queryPage(String sql, Map<String, Object> params, @NonNull PageQuery pageQuery, boolean logSql, @NonNull Class<T> rowMapperClass) {
        validateSql(sql);
        params = ensureParamsNotNull(params);
        pageQuery.validate();

        long start = System.nanoTime();
        Exception exception = null;

        try {
            // 查询总记录数
            String countSql = buildCountSql(sql);
            Long total = this.queryLong(countSql, params, logSql);
            if (total == null || total == 0) {
                return PageResponse.empty(pageQuery.getCurrent(), pageQuery.getSize());
            }

            // 构建分页 SQL
            String pageSql = buildPageSql(sql, pageQuery);

            // 查询分页数据
            List<T> records = this.queryList(pageSql, params, logSql, rowMapperClass);

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("queryPageTyped", sql, params, elapsedMs, null, records.size(), null);
            }

            return PageResponse.of(records, pageQuery, total);
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("queryPageTyped", sql, params, elapsedMs, null, 0, exception);
            }
            throw e;
        }
    }

    // ========================= 更新方法 =========================

    /**
     * 更新操作
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 更新行数
     */
    public int update(String sql, Map<String, Object> params) {
        return this.update(sql, params, false);
    }

    /**
     * 更新操作
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 更新行数
     */
    public int update(String sql, Map<String, Object> params, boolean logSql) {
        validateSql(sql);
        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Exception exception = null;

        try {
            int rows = this.namedParameterJdbcTemplate.update(sql, params);

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("update", sql, params, elapsedMs, rows, null, null);
            }

            return rows;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("update", sql, params, elapsedMs, 0, null, exception);
            }
            throw e;
        }
    }

    /**
     * 更新并返回主键
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 主键
     */
    public long updateAndGetKey(String sql, SqlParameterSource params) {
        return this.updateAndGetKey(sql, params, false);
    }

    /**
     * 更新并返回主键
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 主键
     */
    public long updateAndGetKey(String sql, SqlParameterSource params, boolean logSql) {
        validateSql(sql);

        long start = System.nanoTime();
        Exception exception = null;

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int rows = this.namedParameterJdbcTemplate.update(sql, params, keyHolder);

            if (keyHolder.getKey() == null) {
                if (logSql) {
                    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                    this.logSqlStructured("updateAndGetKey", sql, params, elapsedMs, rows, null, new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "获取主键失败"));
                }
                log.error("获取主键失败，sql: {}, params: {}", sql, params);
                throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "获取主键失败");
            }

            long resultKey = keyHolder.getKey().longValue();

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("updateAndGetKey", sql, params, elapsedMs, rows, null, null);
            }

            return resultKey;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("updateAndGetKey", sql, params, elapsedMs, 0, null, exception);
            }
            throw e;
        }
    }

    /**
     * 批量更新
     *
     * @param sql    SQL 语句
     * @param params 参数数组
     * @return 更新行数数组
     */
    public int[] batchUpdate(String sql, Map<String, ?>[] params) {
        return this.batchUpdate(sql, params, false);
    }

    /**
     * 批量更新
     *
     * @param sql    SQL 语句
     * @param params 参数数组
     * @param logSql 是否记录 SQL 日志
     * @return 更新行数数组
     */
    public int[] batchUpdate(String sql, Map<String, ?>[] params, boolean logSql) {
        validateSql(sql);

        if (params == null || params.length == 0) {
            return new int[0];
        }

        long start = System.nanoTime();
        Exception exception = null;

        try {
            int[] result = this.namedParameterJdbcTemplate.batchUpdate(sql, params);

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                int totalRows = 0;
                for (int rows : result) {
                    totalRows += rows;
                }
                this.logSqlStructured("batchUpdate", sql, "batchSize=" + params.length, elapsedMs, totalRows, null, null);
            }

            return result;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("batchUpdate", sql, "batchSize=" + params.length, elapsedMs, 0, null, exception);
            }
            throw e;
        }
    }

    /**
     * 批量更新
     *
     * @param sql    SQL 语句
     * @param params 参数数组
     */
    public int[] batchUpdate(String sql, SqlParameterSource[] params) {
        return this.batchUpdate(sql, params, false);
    }

    /**
     * 批量更新
     *
     * @param sql    SQL 语句
     * @param params 参数数组
     * @param logSql 是否记录 SQL 日志
     */
    public int[] batchUpdate(String sql, SqlParameterSource[] params, boolean logSql) {
        validateSql(sql);

        if (params == null || params.length == 0) {
            return new int[0];
        }

        long start = System.nanoTime();
        Exception exception = null;

        try {
            int[] result = this.namedParameterJdbcTemplate.batchUpdate(sql, params);

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                int totalRows = 0;
                for (int rows : result) {
                    totalRows += rows;
                }
                this.logSqlStructured("batchUpdate", sql, "batchSize=" + params.length, elapsedMs, totalRows, null, null);
            }

            return result;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("batchUpdate", sql, "batchSize=" + params.length, elapsedMs, 0, null, exception);
            }
            throw e;
        }
    }

    // ========================= 便捷方法（语义化） =========================

    /**
     * 新增操作
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 新增行数
     */
    public int insert(String sql, Map<String, Object> params) {
        return this.insert(sql, params, false);
    }

    /**
     * 新增操作
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 新增行数
     */
    public int insert(String sql, Map<String, Object> params, boolean logSql) {
        return this.update(sql, params, logSql);
    }

    /**
     * 新增并返回主键
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 主键
     */
    public long insertAndGetKey(String sql, SqlParameterSource params) {
        return this.insertAndGetKey(sql, params, false);
    }

    /**
     * 新增并返回主键
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 主键
     */
    public long insertAndGetKey(String sql, SqlParameterSource params, boolean logSql) {
        return this.updateAndGetKey(sql, params, logSql);
    }

    /**
     * 删除操作
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 删除行数
     */
    public int delete(String sql, Map<String, Object> params) {
        return this.delete(sql, params, false);
    }

    /**
     * 删除操作
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param logSql 是否记录 SQL 日志
     * @return 删除行数
     */
    public int delete(String sql, Map<String, Object> params, boolean logSql) {
        return this.update(sql, params, logSql);
    }

    // ========================= 存储过程调用 =========================

    /**
     * 调用存储过程
     *
     * @param procedureName 存储过程名称
     * @param params        参数
     * @return 执行结果
     */
    public Map<String, Object> callProcedure(String procedureName, Map<String, Object> params) {
        return this.callProcedure(procedureName, params, false);
    }

    /**
     * 调用存储过程
     *
     * @param procedureName 存储过程名称
     * @param params        参数
     * @param logSql        是否记录 SQL 日志
     * @return 执行结果
     */
    public Map<String, Object> callProcedure(String procedureName, Map<String, Object> params, boolean logSql) {
        if (procedureName == null || procedureName.trim().isEmpty()) {
            throw new IllegalArgumentException("存储过程名称不能为空");
        }

        params = ensureParamsNotNull(params);

        long start = System.nanoTime();
        Exception exception = null;

        try {
            String callSql = buildCallProcedureSql(procedureName, params);

            Map<String, Object> result = this.namedParameterJdbcTemplate.getJdbcTemplate().execute(
                    callSql,
                    (CallableStatementCallback<Map<String, Object>>) cs -> {
                        Map<String, Object> outParams = new LinkedHashMap<>();
                        cs.execute();
                        // 根据需要获取输出参数, 这里全部获取由业务层自行提取
                        return outParams;
                    }
            );

            if (logSql) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                this.logSqlStructured("callProcedure", callSql, params, elapsedMs, null, 1, null);
            }

            return result;
        } catch (Exception e) {
            exception = e;
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (logSql) {
                this.logSqlStructured("callProcedure", procedureName, params, elapsedMs, null, 0, exception);
            }
            log.error("调用存储过程失败: {}, params: {}", procedureName, params, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "调用存储过程失败");
        }
    }

    // ========================= 事务操作 =========================

    /**
     * 获取事务状态，调用此方法会创建一个新的事务，并返回事务状态
     *
     * @return 事务状态
     */
    public TransactionStatus getTransactionStatus() {
        ensureTransactionManagerInitialized();
        if (this.dataSourceTransactionManager != null) {
            return this.dataSourceTransactionManager.getTransaction(transactionDefinition);
        }
        return null;
    }

    /**
     * 提交事务
     *
     * @param transactionStatus 事务状态
     */
    public void commit(TransactionStatus transactionStatus) {
        ensureTransactionManagerInitialized();
        if (transactionStatus == null) {
            throw new IllegalArgumentException("事务状态不能为空");
        }
        if (this.dataSourceTransactionManager != null) {
            this.dataSourceTransactionManager.commit(transactionStatus);
        }
    }

    /**
     * 回滚事务
     *
     * @param transactionStatus 事务状态
     */
    public void rollback(TransactionStatus transactionStatus) {
        ensureTransactionManagerInitialized();
        if (transactionStatus == null) {
            throw new IllegalArgumentException("事务状态不能为空");
        }
        if (this.dataSourceTransactionManager != null) {
            this.dataSourceTransactionManager.rollback(transactionStatus);
        }
    }

    /**
     * 在事务中执行操作
     *
     * @param callback 事务回调
     * @param <T>      返回类型
     * @return 执行结果
     */
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("事务回调不能为空");
        }

        ensureTransactionManagerInitialized();

        TransactionStatus status = getTransactionStatus();
        try {
            T result = callback.doInTransaction(this);
            commit(status);
            return result;
        } catch (Exception e) {
            rollback(status);
            log.error("事务执行失败，已回滚", e);
            if (e instanceof SystemException systemException) {
                throw systemException;
            } else if (e instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "事务执行失败: " + e.getMessage());
        }
    }

    // ========================= 辅助方法 =========================

    /**
     * 验证 SQL 语句
     *
     * @param sql SQL 语句
     */
    private void validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL 语句不能为空");
        }
    }

    /**
     * 确保参数不为 null
     *
     * @param params 参数
     * @return 非 null 的参数
     */
    private Map<String, Object> ensureParamsNotNull(Map<String, Object> params) {
        return params != null ? params : new LinkedHashMap<>();
    }

    /**
     * 确保事务管理器已初始化
     */
    private void ensureTransactionManagerInitialized() {
        if (this.dataSourceTransactionManager == null || this.transactionDefinition == null) {
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "事务管理器未初始化，请使用事务构造函数创建实例");
        }
    }

    /**
     * 转换列名为小写
     *
     * @param resultList 结果列表
     * @param nullValue  空值替换值
     * @return 转换后的列表
     */
    private List<Map<String, Object>> transformColumnNamesToLowerCase(List<Map<String, Object>> resultList, Object nullValue) {
        if (resultList == null || resultList.isEmpty()) {
            return resultList;
        }

        List<Map<String, Object>> transformed = new ArrayList<>(resultList.size());
        for (Map<String, Object> map : resultList) {
            transformed.add(transformMapKeysToLowerCase(map, nullValue));
        }

        return transformed;
    }

    /**
     * 转换 Map 键为小写
     *
     * @param map 原始 Map
     * @return 转换后的 Map
     */
    private Map<String, Object> transformMapKeysToLowerCase(Map<String, Object> map) {
        return transformMapKeysToLowerCase(map, null);
    }

    /**
     * 转换 Map 键为小写并替换空值
     *
     * @param map       原始 Map
     * @param nullValue 空值替换值
     * @return 转换后的 Map
     */
    private Map<String, Object> transformMapKeysToLowerCase(Map<String, Object> map, Object nullValue) {
        if (map == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> lowerMap = LinkedHashMap.newLinkedHashMap(map.size());
        Set<String> lowerKeys = enableColumnConflictCheck ? new HashSet<>() : null;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String originalKey = entry.getKey();
            String lowerKey = originalKey.toLowerCase();
            Object value = entry.getValue();

            if (nullValue != null && value == null) {
                value = nullValue;
            }

            if (enableColumnConflictCheck) {
                if (lowerKeys.contains(lowerKey) && !lowerKey.equals(originalKey)) {
                    log.error("列名大小写冲突，可能导致数据丢失: 原始键 {} 与 转小写键 {} 发生覆盖", originalKey, lowerKey);
                    throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "列名大小写冲突: " + originalKey + " -> " + lowerKey);
                }
                lowerKeys.add(lowerKey);
            }

            lowerMap.put(lowerKey, value);
        }

        return lowerMap;
    }

    /**
     * 替换空值
     *
     * @param resultList 结果列表
     * @param nullValue  空值替换值
     * @return 处理后的列表
     */
    private List<Map<String, Object>> replaceNullValues(List<Map<String, Object>> resultList, Object nullValue) {
        if (resultList == null || resultList.isEmpty() || nullValue == null) {
            return resultList;
        }

        List<Map<String, Object>> transformed = new ArrayList<>(resultList.size());
        for (Map<String, Object> map : resultList) {
            Map<String, Object> newMap = LinkedHashMap.newLinkedHashMap(map.size());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                newMap.put(entry.getKey(), value == null ? nullValue : value);
            }
            transformed.add(newMap);
        }

        return transformed;
    }

    /**
     * 构建统计 SQL
     *
     * @param sql 原始 SQL
     * @return 统计 SQL
     */
    private String buildCountSql(String sql) {
        String upperSql = sql.toUpperCase().trim();

        // 简单处理，移除 ORDER BY 子句
        int orderByIndex = upperSql.lastIndexOf("ORDER BY");
        if (orderByIndex > 0) {
            sql = sql.substring(0, orderByIndex);
        }

        return "SELECT COUNT(*) FROM (" + sql + ") tmp_count";
    }

    /**
     * 构建分页 SQL
     *
     * @param sql       原始 SQL
     * @param pageQuery 分页参数
     * @return 分页 SQL
     */
    private String buildPageSql(String sql, PageQuery pageQuery) {
        if (Objects.isNull(databaseDialect)) {
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR, "数据库方言未初始化");
        }

        StringBuilder sb = new StringBuilder(sql);

        List<SortOrder> orderList = pageQuery.getOrderList();
        if (orderList != null && !orderList.isEmpty()) {
            List<String> allowedColumns = orderList.stream()
                    .map(SortOrder::getColumn)
                    .toList();
            String orderByClause = pageQuery.buildOrderByClause(allowedColumns);
            if (orderByClause != null && !orderByClause.isEmpty()) {
                sb.append(orderByClause);
            }
        }

        return databaseDialect.getLimitSql(sb.toString(), pageQuery.getOffset(), pageQuery.getLimit());
    }

    /**
     * 构建存储过程调用 SQL
     *
     * @param procedureName 存储过程名称
     * @param params        参数
     * @return 调用 SQL
     */
    private String buildCallProcedureSql(String procedureName, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("{CALL ").append(procedureName).append("(");

        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("?");
            }
        }

        sb.append(")}");
        return sb.toString();
    }

    /**
     * 记录 SQL 日志
     *
     * @param operation    操作
     * @param sql          SQL 语句
     * @param params       参数
     * @param elapsedMs    执行时间（毫秒）
     * @param rowsAffected 受影响行数
     * @param resultSize   返回结果集大小
     * @param exception    异常信息
     */
    private void logSqlStructured(String operation, String sql, Object params, long elapsedMs, Integer rowsAffected, Integer resultSize, Exception exception) {
        // 检查是否为慢查询
        if (elapsedMs > slowQueryThresholdMs) {
            log.warn("检测到慢查询: operation={}, sql={}, elapsedMs={}ms", operation, sql, elapsedMs);
        }

        if (LogFormatType.JSON.getValue().equalsIgnoreCase(logFormat)) {
            logSqlAsJson(operation, sql, params, elapsedMs, rowsAffected, resultSize, exception);
        } else if (LogFormatType.TEXT.getValue().equalsIgnoreCase(logFormat)) {
            logSqlAsText(operation, sql, params, elapsedMs, rowsAffected, resultSize, exception);
        }
    }

    /**
     * 以文本格式记录 SQL 日志
     *
     * @param operation    操作
     * @param sql          SQL 语句
     * @param params       参数
     * @param elapsedMs    执行时间（毫秒）
     * @param rowsAffected 受影响行数
     * @param resultSize   返回结果集大小
     * @param exception    异常信息
     */
    private void logSqlAsText(String operation, String sql, Object params, long elapsedMs, Integer rowsAffected, Integer resultSize, Exception exception) {
        String logMessage = String.format(
                """
                        SQL execution log: ⬇
                        method=[%s],
                        sql=[%s],
                        params=[%s],
                        elapsedMs=[%dms],
                        rowsAffected=[%s],
                        resultSize=[%s],
                        error=[%s]
                        """,
                operation,
                toOneLine(sql),
                sanitizeParams(params),
                elapsedMs,
                rowsAffected,
                resultSize,
                exception != null ? exception.getClass().getSimpleName() + ": " + exception.getMessage() : null
        );

        if (exception != null) {
            log.error(logMessage, exception);
        } else if (elapsedMs > slowQueryThresholdMs) {
            log.warn(logMessage);
        } else {
            log.info(logMessage);
        }
    }

    /**
     * 将多行文本转换为单行文本
     *
     * @param text 多行文本
     * @return 单行文本
     */
    private static String toOneLine(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        return text.replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 以 JSON 格式记录 SQL 日志
     *
     * @param operation    操作
     * @param sql          SQL 语句
     * @param params       参数
     * @param elapsedMs    执行时间（毫秒）
     * @param rowsAffected 受影响行数
     * @param resultSize   返回结果集大小
     * @param exception    异常信息
     */
    private void logSqlAsJson(String operation, String sql, Object params, long elapsedMs, Integer rowsAffected, Integer resultSize, Exception exception) {
        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("operation", operation);
        logData.put("sql", sql);
        logData.put("params", sanitizeParams(params));
        logData.put("elapsedMs", elapsedMs);
        logData.put("rowsAffected", rowsAffected);
        logData.put("resultSize", resultSize);

        if (exception != null) {
            Map<String, Object> errorInfo = new LinkedHashMap<>();
            errorInfo.put("type", exception.getClass().getSimpleName());
            errorInfo.put("message", exception.getMessage());
            logData.put("error", errorInfo);
        }

        try {
            String jsonLog = OBJECT_MAPPER.writeValueAsString(logData);
            if (exception != null) {
                log.error(jsonLog, exception);
            } else if (elapsedMs > slowQueryThresholdMs) {
                log.warn(jsonLog);
            } else {
                log.info(jsonLog);
            }
        } catch (Exception e) {
            log.error("记录 JSON 日志失败", e);
        }
    }

    /**
     * 对参数进行脱敏处理
     *
     * @param paramsObj 参数对象
     * @return 脱敏后的参数对象
     */
    private Object sanitizeParams(Object paramsObj) {
        if (sensitiveDataMasker == null) {
            return paramsObj;
        }

        if (paramsObj instanceof Map<?, ?> original) {
            Map<Object, Object> sanitized = LinkedHashMap.newLinkedHashMap(original.size());

            for (Map.Entry<?, ?> entry : original.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();

                if (sensitiveDataMasker.isSensitive(key)) {
                    sanitized.put(key, sensitiveDataMasker.mask(value));
                } else {
                    sanitized.put(key, value);
                }
            }

            return sanitized;
        } else if (paramsObj instanceof SqlParameterSource) {
            // SqlParameterSource 不便于脱敏，返回类型信息
            return "SqlParameterSource[" + paramsObj.getClass().getSimpleName() + "]";
        }

        return paramsObj;
    }
}
