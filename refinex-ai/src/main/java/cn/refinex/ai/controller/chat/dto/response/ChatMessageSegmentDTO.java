package cn.refinex.ai.controller.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消息段 DTO
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息内容段")
public class ChatMessageSegmentDTO {

    @Schema(description = "段类型: text/reasoning/tool 等")
    private String type;

    @Schema(description = "文本内容")
    private String text;

    @Schema(description = "元数据")
    private Map<String, Object> metadata;
}
