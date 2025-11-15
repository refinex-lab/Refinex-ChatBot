package cn.refinex.kb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 知识入库任务状态
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum KbIngestJobStatus {

    PENDING("PENDING", "待处理"),
    RUNNING("RUNNING", "进行中"),
    DONE("DONE", "完成"),
    FAILED("FAILED", "失败");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据状态码获取状态
     *
     * @param code 状态码
     * @return 状态
     */
    public static KbIngestJobStatus fromCode(String code) {
        for (KbIngestJobStatus t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown KbIngestJobStatus: " + code);
    }
}

