package cn.refinex.ai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 消息内容段
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息内容段")
public class ChatMessageSegment implements Serializable {

    @Schema(description = "类型: text/reasoning/tool/other")
    private String type;

    @Schema(description = "文本内容")
    private String text;

    @Schema(description = "扩展元数据")
    private Map<String, Object> metadata;
}
