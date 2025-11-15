package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 向量库类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum VectorStoreType {

    MILVUS("milvus", "Milvus"),
    QDRANT("qdrant", "Qdrant"),
    REDIS("redis", "Redis"),
    PGVECTOR("pgvector", "Postgres pgvector"),
    ES("es", "Elasticsearch"),
    PINECONE("pinecone", "Pinecone"),
    CUSTOM("custom", "自定义/其他");

    /**
     * 向量库类型代码
     */
    private final String code;

    /**
     * 向量库类型描述
     */
    private final String description;

    /**
     * 根据代码获取向量库类型
     *
     * @param code 向量库类型代码
     * @return 向量库类型
     */
    public static VectorStoreType fromCode(String code) {
        for (VectorStoreType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown VectorStoreType: " + code);
    }
}

