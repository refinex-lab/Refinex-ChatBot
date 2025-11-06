package cn.refinex.core.api;

import cn.refinex.core.domain.PageQuery;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页响应体
 * <p>
 * 用于封装分页查询的结果数据，包含分页信息和数据列表。设计参考 Spring Data 的 Page 接口，
 * 提供丰富的分页元数据信息，便于前端进行分页展示和交互。
 * <p>
 * 使用 record 类实现，保证不可变性和线程安全性。分页响应包含当前页码、每页大小、总记录数、
 * 总页数等核心分页信息，以及实际的数据列表。
 *
 * @param <T> 分页数据的类型
 * @author Refinex
 * @since 1.0.0
 */
public record PageResponse<T>(
        List<T> records,
        long current,
        long size,
        long total,
        long pages,
        boolean hasNext,
        boolean hasPrevious
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建分页响应（基础构造方法）
     * <p>
     * 根据提供的数据列表、当前页码、每页大小和总记录数创建分页响应对象。
     * 该方法会自动计算总页数、是否有下一页、是否有上一页等派生信息。
     *
     * @param records 当前页的数据列表，不可为 null，如果没有数据应传入空列表
     * @param current 当前页码，从 1 开始计数
     * @param size    每页显示的记录数
     * @param total   总记录数
     * @param <T>     数据类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> of(List<T> records, long current, long size, long total) {
        long pages = size == 0 ? 0 : (total + size - 1) / size;
        boolean hasNext = current < pages;
        boolean hasPrevious = current > 1;

        return new PageResponse<>(
                records != null ? records : Collections.emptyList(),
                current,
                size,
                total,
                pages,
                hasNext,
                hasPrevious
        );
    }

    /**
     * 创建分页响应（从分页查询参数）
     * <p>
     * 根据分页查询参数对象创建分页响应，这是推荐的创建方式，可以确保请求参数和响应参数的一致性。
     *
     * @param records   当前页的数据列表
     * @param pageQuery 分页查询参数对象
     * @param total     总记录数
     * @param <T>       数据类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> of(List<T> records, PageQuery pageQuery, long total) {
        return of(records, pageQuery.getCurrent(), pageQuery.getSize(), total);
    }

    /**
     * 创建空分页响应
     * <p>
     * 创建一个不包含任何数据的分页响应对象，通常用于查询结果为空的场景。
     * 该方法返回的分页响应中，数据列表为空列表，总记录数为 0，总页数为 0。
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param <T>     数据类型
     * @return 空分页响应对象
     */
    public static <T> PageResponse<T> empty(long current, long size) {
        return of(Collections.emptyList(), current, size, 0);
    }

    /**
     * 创建空分页响应（从分页查询参数）
     * <p>
     * 根据分页查询参数创建空分页响应对象，这是处理空查询结果的推荐方式。
     *
     * @param pageQuery 分页查询参数对象
     * @param <T>       数据类型
     * @return 空分页响应对象
     */
    public static <T> PageResponse<T> empty(PageQuery pageQuery) {
        return empty(pageQuery.getCurrent(), pageQuery.getSize());
    }

    /**
     * 转换分页响应的数据类型
     * <p>
     * 将当前分页响应中的数据类型转换为另一种类型，分页信息保持不变。
     * 这在需要将实体对象转换为 DTO 对象时非常有用，可以保持分页信息的完整性。
     *
     * @param converter 类型转换函数
     * @param <R>       目标数据类型
     * @return 转换后的分页响应对象
     */
    public <R> PageResponse<R> map(Function<? super T, ? extends R> converter) {
        List<R> convertedRecords = records.stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageResponse<>(
                convertedRecords,
                current,
                size,
                total,
                pages,
                hasNext,
                hasPrevious
        );
    }

    /**
     * 判断是否为空分页
     * <p>
     * 判断当前分页响应是否不包含任何数据记录。
     *
     * @return 如果数据列表为空，则返回 true
     */
    public boolean isEmpty() {
        return records.isEmpty();
    }

    /**
     * 判断是否为首页
     * <p>
     * 判断当前页是否为第一页。
     *
     * @return 如果当前页码为 1，则返回 true
     */
    public boolean isFirst() {
        return current == 1;
    }

    /**
     * 判断是否为末页
     * <p>
     * 判断当前页是否为最后一页。
     *
     * @return 如果当前页码等于总页数，则返回 true
     */
    public boolean isLast() {
        return current >= pages;
    }

    /**
     * 获取数据记录数量
     * <p>
     * 返回当前页实际包含的数据记录数量，注意这可能小于每页大小设置。
     *
     * @return 当前页的记录数
     */
    public int getNumberOfElements() {
        return records.size();
    }

}
