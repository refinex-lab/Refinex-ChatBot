package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.prompt.dto.request.AiPromptCreateRequestDTO;
import cn.refinex.ai.controller.prompt.dto.request.AiPromptPageRequest;
import cn.refinex.ai.controller.prompt.dto.request.AiPromptUpdateRequestDTO;
import cn.refinex.ai.controller.prompt.dto.response.AiPromptResponseDTO;
import cn.refinex.ai.converter.AiPromptConverter;
import cn.refinex.ai.entity.AiPrompt;
import cn.refinex.ai.enums.MessageRole;
import cn.refinex.ai.enums.PromptTemplateFormat;
import cn.refinex.ai.repository.AiPromptRepository;
import cn.refinex.ai.service.AiPromptService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.json.util.JsonUtils;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * AI 提示词服务实现
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiPromptServiceImpl implements AiPromptService {

    /**
     * 默认分类 general
     */
    private static final String DEFAULT_CATEGORY = "general";

    /**
     * 默认模板格式 spring
     */
    private static final PromptTemplateFormat DEFAULT_TEMPLATE_FORMAT = PromptTemplateFormat.SPRING;

    /**
     * 默认角色 system
     */
    private static final MessageRole DEFAULT_ROLE = MessageRole.SYSTEM;

    private final AiPromptRepository repository;
    private final JdbcTemplateManager jdbcManager;
    private final JsonUtils jsonUtils;
    private final AiPromptConverter converter;

    /**
     * 分页查询提示词
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Override
    public PageResponse<AiPromptResponseDTO> page(AiPromptPageRequest request) {
        AiPromptPageRequest query = Objects.isNull(request) ? new AiPromptPageRequest() : request;
        String category = trimToNull(query.getCategory());
        String templateFormat = filterTemplateFormat(query.getTemplateFormat());
        String keyword = trimToNull(query.getKeyword());
        Long userId = LoginHelper.getUserId();
        PageResponse<AiPrompt> page = repository.page(category, query.getStatus(), templateFormat, keyword, query, userId);
        return page.map(converter::toResponse);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 提示词
     */
    @Override
    public Optional<AiPromptResponseDTO> findById(Long id) {
        Long userId = LoginHelper.getUserId();
        return repository.findById(id, userId).map(converter::toResponse);
    }

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return 提示词
     */
    @Override
    public Optional<AiPromptResponseDTO> findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }

        Long userId = LoginHelper.getUserId();
        return repository.findByCode(code.trim(), userId).map(converter::toResponse);
    }

    /**
     * 创建提示词
     *
     * @param request    创建请求
     * @param operatorId 操作人
     */
    @Override
    public void create(AiPromptCreateRequestDTO request, Long operatorId) {
        if (repository.findByCode(request.promptCode().trim(), operatorId).isPresent()) {
            throw new BusinessException("提示词编码已存在: " + request.promptCode());
        }

        AiPrompt entity = buildCreateEntity(request, operatorId);
        jdbcManager.executeInTransaction(jdbc -> {
            long id = repository.insert(entity, jdbc);
            entity.setId(id);
            return null;
        });
    }

    /**
     * 更新提示词
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人
     */
    @Override
    public void update(Long id, AiPromptUpdateRequestDTO request, Long operatorId) {
        AiPrompt exist = repository.findById(id, operatorId).orElseThrow(() -> new BusinessException("提示词不存在或已删除"));
        String newCode = request.promptCode().trim();
        if (!exist.getPromptCode().equals(newCode) && repository.findByCode(newCode, operatorId).isPresent()) {
            throw new BusinessException("提示词编码已存在: " + newCode);
        }

        AiPrompt entity = buildUpdateEntity(exist, request, operatorId);
        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(entity, jdbc);
            return null;
        });
    }

    /**
     * 删除提示词
     *
     * @param id         主键
     * @param operatorId 操作人
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiPrompt exist = repository.findById(id, operatorId).orElseThrow(() -> new BusinessException("提示词不存在或已删除"));
        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(exist.getId(), operatorId, jdbc);
            return null;
        });
    }

    /**
     * 构建创建实体
     *
     * @param request    创建请求
     * @param operatorId 操作人
     * @return 提示词实体
     */
    private AiPrompt buildCreateEntity(AiPromptCreateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        String category = normalizeCategory(request.category());
        String templateFormat = normalizeTemplateFormat(request.templateFormat());
        String role = normalizeRole(request.role());
        String template = request.template().trim();
        String variables = toJsonOrNull(request.variables());
        String examples = toJsonOrNull(request.examples());
        String inputSchema = toJsonOrNull(request.inputSchema());
        return AiPrompt.builder()
                .promptCode(request.promptCode().trim())
                .promptName(request.promptName().trim())
                .category(category)
                .description(trimToNull(request.description()))
                .templateFormat(templateFormat)
                .role(role)
                .template(template)
                .variables(variables)
                .examples(examples)
                .hashSha256(computeHash(templateFormat, role, template, variables, examples, inputSchema))
                .inputSchema(inputSchema)
                .status(normalizeStatus(request.status(), 1))
                .remark(trimToNull(request.remark()))
                .createBy(operatorId)
                .createTime(now)
                .updateBy(operatorId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 构建更新实体
     *
     * @param exist      存在的提示词实体
     * @param request    更新请求
     * @param operatorId 操作人
     * @return 提示词实体
     */
    private AiPrompt buildUpdateEntity(AiPrompt exist, AiPromptUpdateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        String category = resolveCategory(request.category(), exist.getCategory());
        String templateFormat = resolveTemplateFormat(request.templateFormat(), exist.getTemplateFormat());
        String role = resolveRole(request.role(), exist.getRole());
        String variables = request.variables() == null ? exist.getVariables() : toJsonOrNull(request.variables());
        String examples = request.examples() == null ? exist.getExamples() : toJsonOrNull(request.examples());
        String inputSchema = request.inputSchema() == null ? exist.getInputSchema() : toJsonOrNull(request.inputSchema());
        AiPrompt prompt = new AiPrompt();
        prompt.setId(exist.getId());
        prompt.setPromptCode(request.promptCode().trim());
        prompt.setPromptName(request.promptName().trim());
        prompt.setCategory(category);
        prompt.setDescription(request.description() == null ? exist.getDescription() : trimToNull(request.description()));
        prompt.setTemplateFormat(templateFormat);
        prompt.setRole(role);
        prompt.setTemplate(request.template().trim());
        prompt.setVariables(variables);
        prompt.setExamples(examples);
        prompt.setInputSchema(inputSchema);
        prompt.setHashSha256(computeHash(templateFormat, role, prompt.getTemplate(), variables, examples, inputSchema));
        prompt.setStatus(normalizeStatus(request.status(), exist.getStatus() == null ? 1 : exist.getStatus()));
        prompt.setRemark(request.remark() == null ? exist.getRemark() : trimToNull(request.remark()));
        prompt.setCreateBy(exist.getCreateBy());
        prompt.setCreateTime(exist.getCreateTime());
        prompt.setUpdateBy(operatorId);
        prompt.setUpdateTime(now);
        prompt.setDeleted(exist.getDeleted());
        prompt.setDeleteBy(exist.getDeleteBy());
        prompt.setDeleteTime(exist.getDeleteTime());
        return prompt;
    }

    /**
     * 归一化分类
     *
     * @param category 分类
     * @return 归一化后的分类
     */
    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return DEFAULT_CATEGORY;
        }
        return category.trim();
    }

    /**
     * 归一化模板格式
     *
     * @param templateFormat 模板格式
     * @return 归一化后的模板格式
     */
    private String normalizeTemplateFormat(String templateFormat) {
        return parseTemplateFormat(templateFormat).getCode();
    }

    /**
     * 归一化角色
     *
     * @param role 角色
     * @return 归一化后的角色
     */
    private String normalizeRole(String role) {
        return parseRole(role).getCode();
    }

    /**
     * 解析分类
     *
     * @param candidate 候选分类
     * @param current   当前分类
     * @return 解析后的分类
     */
    private String resolveCategory(String candidate, String current) {
        if (StringUtils.hasText(candidate)) {
            return candidate.trim();
        }
        return StringUtils.hasText(current) ? current : DEFAULT_CATEGORY;
    }

    /**
     * 解析模板格式
     *
     * @param candidate 候选模板格式
     * @param current   当前模板格式
     * @return 解析后的模板格式
     */
    private String resolveTemplateFormat(String candidate, String current) {
        if (StringUtils.hasText(candidate)) {
            return parseTemplateFormat(candidate).getCode();
        }
        if (StringUtils.hasText(current)) {
            return parseTemplateFormat(current).getCode();
        }
        return DEFAULT_TEMPLATE_FORMAT.getCode();
    }

    /**
     * 解析角色
     *
     * @param candidate 候选角色
     * @param current   当前角色
     * @return 解析后的角色
     */
    private String resolveRole(String candidate, String current) {
        if (StringUtils.hasText(candidate)) {
            return parseRole(candidate).getCode();
        }
        if (StringUtils.hasText(current)) {
            return parseRole(current).getCode();
        }
        return DEFAULT_ROLE.getCode();
    }

    /**
     * 归一化状态
     *
     * @param status       状态
     * @param defaultStatus 默认状态
     * @return 归一化后的状态
     */
    private int normalizeStatus(Integer status, int defaultStatus) {
        if (status == null) {
            return defaultStatus;
        }
        return status == 1 ? 1 : 0;
    }

    /**
     * 过滤模板格式
     *
     * @param templateFormat 模板格式
     * @return 过滤后的模板格式
     */
    private String filterTemplateFormat(String templateFormat) {
        if (!StringUtils.hasText(templateFormat)) {
            return null;
        }
        return parseTemplateFormat(templateFormat).getCode();
    }

    /**
     * 转换为JSON字符串或null
     *
     * @param value 值
     * @return JSON字符串或null
     */
    private String toJsonOrNull(Object value) {
        return switch (value) {
            case null -> null;
            case Map<?, ?> map when map.isEmpty() -> null;
            case List<?> list when list.isEmpty() -> null;
            default -> jsonUtils.toJson(value);
        };
    }

    /**
     * 计算提示词哈希值
     *
     * @param templateFormat 模板格式
     * @param role           角色
     * @param template       模板
     * @param variablesJson  变量JSON
     * @param examplesJson   示例JSON
     * @param inputSchemaJson 输入模式JSON
     * @return 提示词哈希值
     */
    private String computeHash(String templateFormat, String role, String template, String variablesJson, String examplesJson, String inputSchemaJson) {
        String effectiveFormat = StringUtils.hasText(templateFormat) ? templateFormat : DEFAULT_TEMPLATE_FORMAT.getCode();
        String effectiveRole = StringUtils.hasText(role) ? role : DEFAULT_ROLE.getCode();
        StringBuilder sb = new StringBuilder();
        sb.append(effectiveFormat).append('|')
                .append(effectiveRole).append('|')
                .append(template == null ? "" : template.trim()).append('|')
                .append(variablesJson == null ? "" : variablesJson).append('|')
                .append(examplesJson == null ? "" : examplesJson).append('|')
                .append(inputSchemaJson == null ? "" : inputSchemaJson);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前JVM不支持SHA-256摘要算法", e);
        }
    }

    /**
     * 解析模板格式
     *
     * @param raw 原始模板格式
     * @return 解析后的模板格式
     */
    private PromptTemplateFormat parseTemplateFormat(String raw) {
        if (!StringUtils.hasText(raw)) {
            return DEFAULT_TEMPLATE_FORMAT;
        }
        try {
            return PromptTemplateFormat.fromCode(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("模板格式不支持: " + raw);
        }
    }

    /**
     * 解析角色
     *
     * @param raw 原始角色
     * @return 解析后的角色
     */
    private MessageRole parseRole(String raw) {
        if (!StringUtils.hasText(raw)) {
            return DEFAULT_ROLE;
        }
        try {
            return MessageRole.fromCode(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("角色类型不支持: " + raw);
        }
    }
}
