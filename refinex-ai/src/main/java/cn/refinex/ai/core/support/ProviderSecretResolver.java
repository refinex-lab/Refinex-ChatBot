package cn.refinex.ai.core.support;

import cn.refinex.ai.entity.AiProvider;
import cn.refinex.core.service.CryptoService;
import cn.refinex.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 供应商密钥解析器，负责解密或根据别名查找 API Key
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderSecretResolver {

    private final CryptoService cryptoService;
    private final Environment environment;

    /**
     * 解析 API Key
     *
     * @param provider 供应商
     * @return API Key 明文
     */
    public String resolveApiKey(AiProvider provider) {
        if (provider == null) {
            return null;
        }

        String cipher = StringUtils.trimToNull(provider.getApiKeyCipher());
        if (cipher != null) {
            try {
                return cryptoService.decrypt(cipher);
            } catch (Exception ex) {
                log.warn("解密供应商[{}]API Key 失败，尝试使用原始值", provider.getProviderCode(), ex);
                return cipher;
            }
        }

        String alias = StringUtils.trimToNull(provider.getApiKeyIndex());
        if (alias != null) {
            String value = StringUtils.trimToNull(environment.getProperty(alias));
            if (value == null) {
                value = StringUtils.trimToNull(System.getenv(alias));
            }
            if (value == null) {
                value = StringUtils.trimToNull(System.getProperty(alias));
            }
            if (value != null) {
                return value;
            }
            log.warn("未找到供应商 [{}] API Key, 请检查 alias: {}", provider.getProviderCode(), alias);
        }
        return null;
    }
}
