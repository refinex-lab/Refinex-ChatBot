package cn.refinex.kb.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RAG 知识库
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "RAG 知识库")
public class KbBase extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "知识库编码，唯一")
    private String kbCode;

    @Schema(description = "知识库名称")
    private String kbName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "可见性: PRIVATE/TEAM/ORG/PUBLIC")
    private String visibility;

    @Schema(description = "所有者用户ID")
    private Long ownerUserId;

    @Schema(description = "向量库类型: milvus/qdrant/pgvector/es/pinecone 等")
    private String vectorStoreType;

    @Schema(description = "向量库集合/命名空间")
    private String vectorStoreCollection;

    @Schema(description = "向量库配置(JSON)")
    private String vectorConfig;

    @Schema(description = "默认Embedding模型ID(逻辑关联 ai_model.id)")
    private Long embedModelId;

    @Schema(description = "默认Embedding模型Key")
    private String embedModelKey;

    @Schema(description = "维度(如需要)")
    private Integer dimension;

    @Schema(description = "度量方式: cosine/l2/ip")
    private String metric;

    @Schema(description = "检索策略: VECTOR/BM25/HYBRID/MMR/RRF")
    private String ragStrategy;

    @Schema(description = "默认TopK")
    private Integer defaultTopK;

    @Schema(description = "文档数")
    private Integer docCount;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "删除人ID")
    private Long deleteBy;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "备注")
    private String remark;
}

