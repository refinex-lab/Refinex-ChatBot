package cn.refinex.platform.repository.command;

import lombok.Builder;
import lombok.Data;

/**
 * 更新用户基础信息命令对象，避免方法入参过多
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Builder
public class UpdateBasicInfoCommand {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户呢称
     */
    private String nickname;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户邮箱加密值
     */
    private String emailCipher;

    /**
     * 用户邮箱索引
     */
    private String emailIndex;

    /**
     * 用户手机号加密值
     */
    private String mobileCipher;

    /**
     * 用户手机号索引
     */
    private String mobileIndex;

    /**
     * 更新人
     */
    private Long updateBy;
}

