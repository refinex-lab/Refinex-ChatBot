package cn.refinex.core.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页查询参数
 * <p>
 * 用于接收前端传递的分页查询参数，包含当前页码和每页大小。设计参考 MyBatis Plus 的分页插件，
 * 提供合理的默认值和参数校验规则，确保分页参数的有效性。
 * <p>
 * 该类作为所有分页查询接口的基础参数类，具体的业务查询可以继承此类并添加额外的查询条件字段。
 * 通过 Jakarta Validation 注解进行参数校验，避免非法的分页参数导致的查询问题。
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 默认页码，从第 1 页开始
     */
    private static final long DEFAULT_CURRENT = 1L;

    /**
     * 默认每页大小
     */
    private static final long DEFAULT_SIZE = 10L;

    /**
     * 最大每页大小，防止单次查询数据量过大导致性能问题
     */
    private static final long MAX_SIZE = 500L;

    /**
     * 当前页码，从 1 开始计数
     * <p>
     * 使用 Jakarta Validation 注解进行参数校验，确保页码为正整数。
     * 如果前端未传递该参数，则使用默认值 1。
     */
    @Min(value = 1, message = "页码必须大于等于 1")
    private Long current = DEFAULT_CURRENT;

    /**
     * 每页显示的记录数
     * <p>
     * 使用 Jakarta Validation 注解进行参数校验，确保每页大小在合理范围内。
     * 最小值为 1，最大值为 500，防止单次查询数据量过大。如果前端未传递该参数，则使用默认值 10。
     */
    @Min(value = 1, message = "每页大小必须大于等于 1")
    @Max(value = MAX_SIZE, message = "每页大小不能超过 " + MAX_SIZE)
    private Long size = DEFAULT_SIZE;

    /**
     * 排序字段集合
     * <p>
     * 可从前端传入，例如：
     * <pre>
     * orderList[0].column = "createdTime"
     * orderList[0].asc = false
     * </pre>
     */
    private List<SortOrder> orderList = Collections.emptyList();

    /**
     * 无参构造函数
     * <p>
     * 创建分页查询参数对象，使用默认的页码和每页大小。
     */
    public PageQuery() {
    }

    /**
     * 构造函数（指定页码和每页大小）
     * <p>
     * 创建分页查询参数对象，可以直接指定页码和每页大小。
     * 该构造函数主要用于编程式创建分页参数，例如在服务层进行内部分页查询时使用。
     *
     * @param current 当前页码
     * @param size    每页大小
     */
    public PageQuery(Long current, Long size) {
        this.current = current != null && current > 0 ? current : DEFAULT_CURRENT;
        this.size = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
    }

    /**
     * 创建默认分页查询参数
     * <p>
     * 返回使用默认配置的分页查询参数对象，页码为 1，每页大小为 10。
     * 这是一个便捷的静态工厂方法，用于快速创建默认的分页参数。
     *
     * @return 默认分页查询参数对象
     */
    public static PageQuery of() {
        return new PageQuery();
    }

    /**
     * 创建分页查询参数
     * <p>
     * 根据指定的页码和每页大小创建分页查询参数对象。
     * 该方法会自动进行参数校验和修正，确保参数的有效性。
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页查询参数对象
     */
    public static PageQuery of(Long current, Long size) {
        return new PageQuery(current, size);
    }

    /**
     * 计算偏移量
     * <p>
     * 根据当前页码和每页大小计算数据库查询的偏移量（OFFSET）。
     * 该方法主要用于不支持分页插件的场景，需要手动拼接 SQL 分页语句时使用。
     *
     * @return 偏移量，计算公式为 (current - 1) * size
     */
    public long getOffset() {
        return (current - 1) * size;
    }

    /**
     * 获取限制数量
     * <p>
     * 返回每页大小，主要用于数据库查询的 LIMIT 子句。
     * 该方法是 getSize() 的语义化别名，使代码更具可读性。
     *
     * @return 限制数量，即每页大小
     */
    public long getLimit() {
        return size;
    }

    /**
     * 校验分页参数
     * <p>
     * 校验当前页码和每页大小是否符合规范，确保分页参数的有效性。
     * 如果校验失败，将抛出 IllegalArgumentException 异常。
     * <p>
     * 校验规则：
     * <ul>
     *     <li>当前页码必须大于等于 1</li>
     *     <li>每页大小必须在 1 和 500 之间</li>
     * </ul>
     */
    public void validate() {
        if (current < 1) {
            throw new IllegalArgumentException("页码必须大于等于 1");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("每页大小必须在 1 和 " + MAX_SIZE + " 之间");
        }
    }

    /**
     * 生成排序 SQL 片段
     * <p>
     * 示例返回：ORDER BY created_time DESC, id ASC
     * 适用于 JdbcTemplate / 自定义 SQL 场景。
     *
     * @param allowedColumns 允许排序的列白名单（防 SQL 注入）
     * @return SQL 排序片段（可能为空字符串）
     */
    public String buildOrderByClause(List<String> allowedColumns) {
        if (orderList == null || orderList.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(" ORDER BY ");
        boolean first = true;

        for (SortOrder order : orderList) {
            if (order == null || order.getColumn() == null) {
                continue;
            };

            String column = order.getColumn();
            // 防止 SQL 注入
            if (allowedColumns != null && !allowedColumns.contains(column)) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            sb.append(column).append(order.getAsc() ? " ASC" : " DESC");
            first = false;
        }
        return first ? "" : sb.toString();
    }
}
