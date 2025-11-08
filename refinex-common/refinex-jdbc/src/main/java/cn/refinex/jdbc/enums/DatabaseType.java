package cn.refinex.jdbc.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DatabaseType {

    /**
     * MySQL 数据库
     */
    MYSQL("mysql"),

    /**
     * PostgreSQL 数据库
     */
    POSTGRESQL("postgresql"),

    /**
     * Oracle 数据库
     */
    ORACLE("oracle"),

    ;

    /**
     * 数据库类型值
     */
    private final String value;

    /**
     * 根据数据库类型值获取枚举实例
     *
     * @param value 数据库类型值
     * @return 数据库类型枚举实例
     */
    public static DatabaseType fromValue(String value) {
        for (DatabaseType databaseType : values()) {
            if (databaseType.value.equals(value)) {
                return databaseType;
            }
        }
        throw new IllegalArgumentException("Unknown database type value: " + value);
    }
}
