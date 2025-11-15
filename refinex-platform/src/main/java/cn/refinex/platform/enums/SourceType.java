package cn.refinex.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档来源类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SourceType {

    UPLOAD("UPLOAD", "上传"),
    URL("URL", "URL 抓取"),
    REPO("REPO", "代码仓库"),
    S3("S3", "S3/对象存储"),
    CONFLUENCE("CONFLUENCE", "Confluence"),
    GIT("GIT", "Git 仓库");

    /**
     * 源类型代码
     */
    private final String code;

    /**
     * 源类型描述
     */
    private final String description;

    /**
     * 根据源类型代码获取源类型
     *
     * @param code 源类型代码
     * @return 源类型
     */
    public static SourceType fromCode(String code) {
        for (SourceType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown SourceType: " + code);
    }
}
