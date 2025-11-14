package cn.refinex.platform.entity;

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
 * 系统用户实体类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
//@Builder
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户实体类")
public class SysUser extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "AES-GCM 加密后的手机号")
    private String mobileCipher;

    @Schema(description = "HMAC(手机号) 固定长度哈希值,用于 WHERE 查询匹配")
    private String mobileIndex;

    @Schema(description = "AES-GCM 加密后的邮箱")
    private String emailCipher;

    @Schema(description = "HMAC(邮箱) 固定长度哈希值,用于 WHERE 查询匹配")
    private String emailIndex;

    @Schema(description = "BCrypt加密后的密码哈希")
    private String password;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "性别: MALE, FEMALE, OTHER")
    private String sex;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "账户状态: 1正常, 2冻结, 3注销")
    private Integer accountStatus;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "逻辑删除: 0未删除, 1已删除")
    private Integer deleted;

    @Schema(description = "删除人ID")
    private Long deleteBy;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "启用状态: 1启用, 0停用")
    private Integer status;

}
