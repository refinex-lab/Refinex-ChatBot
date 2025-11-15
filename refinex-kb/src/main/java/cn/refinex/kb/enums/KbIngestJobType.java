package cn.refinex.kb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 知识入库任务类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum KbIngestJobType {

    INGEST("INGEST", "入库"),
    REINDEX("REINDEX", "重建索引"),
    DELETE("DELETE", "删除"),
    REFRESH("REFRESH", "刷新");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static KbIngestJobType fromCode(String code) {
        for (KbIngestJobType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown KbIngestJobType: " + code);
    }
}

