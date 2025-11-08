package cn.refinex.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序字段信息
 * <p>
 * 表示一个排序条件，例如：id DESC 或 name ASC。
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortOrder {

    /**
     * 排序字段（数据库字段名或实体属性名）
     */
    private String column;

    /**
     * 是否升序排序
     */
    private Boolean asc = true;

    /**
     * 构造升序排序
     *
     * @param column 排序字段
     * @return 排序对象
     */
    public static SortOrder asc(String column) {
        return new SortOrder(column, true);
    }

    /**
     * 构造降序排序
     *
     * @param column 排序字段
     * @return 排序对象
     */
    public static SortOrder desc(String column) {
        return new SortOrder(column, false);
    }
}
