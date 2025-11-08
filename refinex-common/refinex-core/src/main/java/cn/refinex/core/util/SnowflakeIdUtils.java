package cn.refinex.core.util;

/**
 * 分布式唯一ID生成器 - Snowflake 算法实现
 * <p>
 * 结构：64 位组成
 * 1位符号位 + 41位时间戳 + 5位数据中心ID + 5位工作节点ID + 12位序列号
 * <p>
 * 特点：
 * - 按时间趋势递增
 * - 保证分布式下唯一性
 * - 每毫秒最多生成 4096 个 ID，由 12 位序列号决定
 * <p>
 * 改进：
 * - 支持时钟回拨容错策略（可配置）
 * - 更优雅的单例与线程安全处理
 *
 * @author Refinex
 * @since 1.0.0
 */
public class SnowflakeIdUtils {

    // ============================== 常量定义 ===================================
    /** 开始时间截 (2015-01-01) */
    private static final long TWEPOCH = 1420041600000L;

    /** 各部分位数 */
    /** 5位工作节点ID */
    private static final long WORKER_ID_BITS = 5L;
    /** 5位数据中心ID */
    private static final long DATACENTER_ID_BITS = 5L;
    /** 12位序列号 */
    private static final long SEQUENCE_BITS = 12L;

    /** 最大值 */
    /** 最大工作节点ID */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    /** 最大数据中心ID */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /** 位移量 */
    /** 工作节点ID左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /** 数据中心ID左移位数 */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    /** 时间戳左移位数 */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /** 序列掩码 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // ============================== 成员变量 ===================================
    /** 工作节点ID */
    private final long workerId;
    /** 数据中心ID */
    private final long datacenterId;
    /** 序列号 */
    private long sequence = 0L;
    /** 上次生成ID的时间戳 */
    private long lastTimestamp = -1L;

    /** 时钟回拨允许的最大容忍毫秒数，默认2ms（可配置） */
    private final long maxBackwardMs;

    // ============================== 构造方法 ===================================

    /**
     * 构造函数
     *
     * @param workerId     工作节点ID，范围 0-31
     * @param datacenterId 数据中心ID，范围 0-31
     */
    public SnowflakeIdUtils(long workerId, long datacenterId) {
        this(workerId, datacenterId, 2L);
    }

    /**
     * 构造函数
     *
     * @param workerId     工作节点ID，范围 0-31
     * @param datacenterId 数据中心ID，范围 0-31
     * @param maxBackwardMs 时钟回拨允许的最大容忍毫秒数，默认2ms（可配置）
     */
    public SnowflakeIdUtils(long workerId, long datacenterId, long maxBackwardMs) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("WorkerId 必须在 0-%d 范围内", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("DatacenterId 必须在 0-%d 范围内", MAX_DATACENTER_ID));
        }

        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.maxBackwardMs = maxBackwardMs;
    }

    // ============================== 生成方法 ===================================

    /**
     * 获取下一个唯一ID（线程安全）
     *
     * @return long 唯一ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 处理时钟回拨: 若当前时间戳小于上次生成ID的时间戳，可能是时钟回拨
        if (timestamp < lastTimestamp) {
            // 时钟回拨检测: 计算回拨偏移量
            long offset = lastTimestamp - timestamp;
            // 若回拨偏移量小于等于最大容忍毫秒数，等待到 lastTimestamp 后继续
            if (offset <= maxBackwardMs) {
                // 等待到 lastTimestamp 后继续
                timestamp = tilNextMillis(lastTimestamp);
            } else {
                // 时钟回拨超出容忍范围，抛出异常
                throw new RuntimeException(String.format("时钟回拨超过容忍范围：%d ms", offset));
            }
        }

        // 同一毫秒内自增序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 序列溢出，等到下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 毫秒变更，序列重置
            sequence = 0L;
        }

        // 更新上次生成ID的时间戳
        lastTimestamp = timestamp;

        // 生成ID: 组合时间戳、数据中心ID、工作节点ID、序列号
        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待直到下一个毫秒，确保生成的ID时间戳是严格递增的
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一个毫秒的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 当前时间（毫秒）
     *
     * @return 当前时间（毫秒）
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
