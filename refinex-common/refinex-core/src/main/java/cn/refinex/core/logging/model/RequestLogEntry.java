package cn.refinex.core.logging.model;

import cn.refinex.core.logging.enums.RequestLogType;
import lombok.Builder;

/**
 * 请求日志实体
 *
 * @author Refinex
 * @since 1.0.0
 */
@Builder
public record RequestLogEntry(

        /*
          服务名称
         */
        String serviceName,

        /*
          日志标题
         */
        String title,

        /*
          日志类型
         */
        RequestLogType type,

        /*
          日志描述
         */
        String description,

        /*
          请求URI
         */
        String requestUri,

        /*
          HTTP方法
         */
        String httpMethod,

        /*
          客户端IP地址
         */
        String clientIp,

        /*
          用户代理信息
         */
        String userAgent,

        /*
          请求参数签名
         */
        String dataSign,

        /*
            请求ID，链路追踪使用
         */
        String traceId,

        /*
          HTTP状态码
         */
        Integer httpStatus,

        /*
          请求是否成功
         */
        boolean success,

        /*
          用户ID
         */
        Long userId,

        /*
          用户名
         */
        String username,

        /*
          控制器类名
         */
        String controller,

        /*
          方法名
         */
        String methodName,

        /*
          请求体
         */
        String requestBody,

        /*
          响应体
         */
        String responseBody,

        /*
          错误信息
         */
        String errorMessage,

        /*
          请求处理时间（毫秒）
         */
        long durationMs,

        /*
          日志生成时间戳
         */
        long timestamp
) { }
