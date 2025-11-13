package cn.refinex.satoken.common.sametoken;

import cn.dev33.satoken.same.SaSameUtil;
import cn.hutool.core.util.StrUtil;
import cn.refinex.core.util.ServletUtils;
import cn.refinex.core.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SAME-Token 工具类
 * <p>
 * SAME-Token(Server And Microservice Exchange Token)是 Sa-Token 提供的微服务间内部调用认证机制。
 * 用于解决微服务间调用时没有用户 Token 的场景, 确保内部服务调用的安全性。
 * <p>
 * 适用场景:
 * 1. 登录/注册接口: 用户尚未获得 Token, 但需要调用其他服务进行数据处理
 * 2. 定时任务: 系统自动执行任务, 无用户上下文, 需要调用其他服务
 * 3. 异步消息处理: MQ 消费者处理消息时需要调用其他服务
 * 4. 内部管理接口: 系统管理员直接调用内部接口
 * 5. 服务间数据同步: 服务之间的数据同步操作
 * <p>
 * 工作原理:
 * 1. 调用方使用共享密钥生成 SAME-Token
 * 2. 将 SAME-Token 放入 HTTP 请求头中
 * 3. 被调用方验证 SAME-Token 的有效性
 * 4. 验证通过后执行业务逻辑
 * <p>
 * 安全性保障:
 * - 基于共享密钥机制, 只有知道密钥的服务才能生成有效 Token
 * - Token 包含时间戳, 有过期时间, 防止重放攻击
 * - 与用户 Token 完全隔离, 避免权限混淆
 * - 仅用于内部服务调用, 不对外暴露
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SameTokenUtil {

    /**
     * 生成 SAME-Token
     * <p>
     * 生成一个用于微服务间调用的 Token。
     * 该 Token 基于配置的密钥生成, 包含时间戳信息。
     *
     * @return SAME-Token
     */
    public static String generateToken() {
        String token = SaSameUtil.getToken();
        log.debug("生成 SAME-Token: token={}", token);
        return token;
    }

    /**
     * 校验 SAME-Token 是否有效
     * <p>
     * 验证提供的 Token 是否合法。
     * 如果 Token 无效会抛出 SaTokenException 异常。
     *
     * @param token SAME-Token
     * @return true-有效, false-无效
     */
    public static boolean checkToken(String token) {
        if (StrUtil.isBlank(token)) {
            log.warn("SAME-Token 为空,校验失败");
            return false;
        }

        try {
            SaSameUtil.checkToken(token);
            log.debug("SAME-Token 校验成功: token={}", token);
            return true;
        } catch (Exception e) {
            log.warn("SAME-Token 校验失败: token={}, error={}", token, e.getMessage());
            return false;
        }
    }

    /**
     * 校验 SAME-Token(如果无效则抛出异常)
     * <p>
     * 与 checkToken 的区别:该方法在校验失败时会抛出异常,而不是返回 false。
     * 适用于必须通过校验才能继续执行的场景。
     *
     * @param token SAME-Token
     * @throws cn.dev33.satoken.exception.SaTokenException Token 无效时抛出
     */
    public static void checkTokenOrThrow(String token) {
        SaSameUtil.checkToken(token);
    }

    /**
     * 判断当前请求是否携带了有效的 SAME-Token
     * <p>
     * 从当前请求的 Header 和 Parameter 中获取 SAME-Token 并进行校验。
     * 优先从 Header 中获取,如果 Header 中没有则从 Parameter 中获取。
     *
     * @return true-当前请求携带有效的 SAME-Token, false-否
     */
    public static boolean isValid() {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            if (request == null) {
                log.debug("当前请求对象为 null");
                return false;
            }

            // SAME-Token 的 key
            String sameTokenKey = SaSameUtil.SAME_TOKEN;

            // 1. 优先从 Header 获取
            String sameTokenValue = request.getHeader(sameTokenKey);

            // 2. 如果 Header 没有，则从 Parameter 获取
            if (StringUtils.isBlank(sameTokenValue)) {
                sameTokenValue = request.getParameter(sameTokenKey);
            }

            // 3. 校验 Token
            boolean valid = SaSameUtil.isValid(sameTokenValue);
            if (!valid) {
                log.debug("SAME-Token 校验失败，token={}", sameTokenValue);
            }

            return valid;
        } catch (Exception e) {
            log.debug("当前请求未携带有效的 SAME-Token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 校验当前请求的 SAME-Token(如果无效则抛出异常)
     * <p>
     * 从当前请求中获取 SAME-Token 并进行校验。
     * 如果校验失败会抛出异常,适用于拦截器或过滤器中使用。
     *
     * @throws cn.dev33.satoken.exception.SaTokenException Token 无效时抛出
     */
    public static void checkCurrentRequestToken() {
        SaSameUtil.checkCurrentRequestToken();
    }

    /**
     * 刷新 SAME-Token
     * <p>
     * 生成一个新的 Token, 同时旧 Token 仍然有效(直到过期)。
     * 建议在长时间运行的任务中定期刷新 Token。
     *
     * @return 新的 SAME-Token
     */
    public static String refreshToken() {
        String newToken = SaSameUtil.refreshToken();
        log.debug("刷新 SAME-Token: newToken={}", newToken);
        return newToken;
    }
}
