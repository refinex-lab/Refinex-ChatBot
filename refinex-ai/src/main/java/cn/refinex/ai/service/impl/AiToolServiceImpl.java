package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.tool.dto.request.AiToolCreateRequestDTO;
import cn.refinex.ai.controller.tool.dto.request.AiToolPageRequest;
import cn.refinex.ai.controller.tool.dto.request.AiToolUpdateRequestDTO;
import cn.refinex.ai.controller.tool.dto.response.AiToolResponseDTO;
import cn.refinex.ai.converter.AiToolConverter;
import cn.refinex.ai.entity.AiMcpServer;
import cn.refinex.ai.entity.AiTool;
import cn.refinex.ai.repository.AiMcpServerRepository;
import cn.refinex.ai.repository.AiToolRepository;
import cn.refinex.ai.service.AiToolService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.json.util.JsonUtils;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * 工具服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiToolServiceImpl implements AiToolService {

    private final AiToolRepository repository;
    private final AiMcpServerRepository mcpServerRepository;
    private final AiToolConverter converter;
    private final JsonUtils jsonUtils;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 分页查询工具
     *
     * @param request 查询请求
     * @return 工具列表
     */
    @Override
    public PageResponse<AiToolResponseDTO> page(AiToolPageRequest request) {
        AiToolPageRequest query = Objects.isNull(request) ? new AiToolPageRequest() : request;
        Long userId = LoginHelper.getUserId();
        PageResponse<AiTool> page = repository.page(
                trimToNull(query.getToolType()), query.getMcpServerId(), query.getStatus(),
                trimToNull(query.getKeyword()), query, userId
        );
        return page.map(converter::toResponse);
    }

    /**
     * 根据 ID 查询工具
     *
     * @param id 主键
     * @return 工具
     */
    @Override
    public Optional<AiToolResponseDTO> findById(Long id) {
        return repository.findById(id, LoginHelper.getUserId()).map(converter::toResponse);
    }

    /**
     * 新增工具
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void create(AiToolCreateRequestDTO request, Long operatorId) {
        String code = request.toolCode().trim();
        if (repository.findByCode(code, operatorId).isPresent()) {
            throw new BusinessException("工具编码已存在: " + code);
        }

        validateMcpServer(request.mcpServerId(), operatorId);
        AiTool entity = buildEntity(request, operatorId);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateBy(operatorId);
        entity.setCreateTime(now);
        entity.setUpdateBy(operatorId);
        entity.setUpdateTime(now);
        entity.setDeleted(0);

        jdbcManager.executeInTransaction(jdbc -> {
            long id = repository.insert(entity, jdbc);
            entity.setId(id);
            return null;
        });
    }

    /**
     * 更新工具
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void update(Long id, AiToolUpdateRequestDTO request, Long operatorId) {
        AiTool exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("工具不存在或已删除"));

        String newCode = request.toolCode().trim();
        if (!exist.getToolCode().equals(newCode) && repository.findByCode(newCode, operatorId).isPresent()) {
            throw new BusinessException("工具编码已存在: " + newCode);
        }

        validateMcpServer(request.mcpServerId(), operatorId);
        AiTool entity = buildEntity(new AiToolCreateRequestDTO(
                request.toolCode(), request.toolName(), request.toolType(), request.implBean(),
                request.endpoint(), request.timeoutMs(), request.inputSchema(), request.outputSchema(),
                request.mcpServerId(), request.status(), request.remark()), operatorId
        );

        entity.setId(exist.getId());
        entity.setCreateBy(exist.getCreateBy());
        entity.setCreateTime(exist.getCreateTime());
        entity.setDeleted(exist.getDeleted());
        entity.setUpdateTime(LocalDateTime.now());

        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(entity, jdbc);
            return null;
        });
    }

    /**
     * 更新工具状态
     *
     * @param id         主键
     * @param status     状态:1启用,0停用
     * @param operatorId 操作人 ID
     */
    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        AiTool exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("工具不存在或已删除"));

        int normalized = normalizeFlag(status, exist.getStatus());
        if (Objects.equals(exist.getStatus(), normalized)) {
            return;
        }

        exist.setStatus(normalized);
        exist.setUpdateBy(operatorId);
        exist.setUpdateTime(LocalDateTime.now());

        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(exist, jdbc);
            return null;
        });
    }

    /**
     * 逻辑删除工具
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiTool exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("工具不存在或已删除"));

        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(exist.getId(), operatorId, jdbc);
            return null;
        });
    }

    /**
     * 构建工具实体
     *
     * @param request    创建请求
     * @param operatorId 操作人 ID
     * @return 工具实体
     */
    private AiTool buildEntity(AiToolCreateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        return AiTool.builder()
                .toolCode(request.toolCode().trim())
                .toolName(request.toolName().trim())
                .toolType(request.toolType().trim())
                .implBean(trimToNull(request.implBean()))
                .endpoint(trimToNull(request.endpoint()))
                .timeoutMs(request.timeoutMs())
                .inputSchema(toJson(request.inputSchema()))
                .outputSchema(toJson(request.outputSchema()))
                .mcpServerId(request.mcpServerId())
                .status(normalizeFlag(request.status(), 1))
                .remark(trimToNull(request.remark()))
                .createBy(operatorId)
                .createTime(now)
                .updateBy(operatorId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 校验 MCP Server 是否存在
     *
     * @param mcpServerId MCP Server 主键
     * @param operatorId  操作人 ID
     */
    private void validateMcpServer(Long mcpServerId, Long operatorId) {
        if (mcpServerId == null) {
            return;
        }
        Optional<AiMcpServer> server = mcpServerRepository.findById(mcpServerId, operatorId);
        if (server.isEmpty()) {
            throw new BusinessException("MCP Server 不存在或已删除");
        }
    }

    /**
     * 将 Map 转换为 JSON 字符串
     *
     * @param map 输入 Map
     * @return JSON 字符串
     */
    private String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return jsonUtils.toJson(map);
    }

    /**
     * 标准化状态标志
     *
     * @param value       输入值
     * @param defaultValue 默认值
     * @return 标准化后的标志
     */
    private int normalizeFlag(Integer value, Integer defaultValue) {
        int fallback = defaultValue == null ? 0 : defaultValue;
        if (value == null) {
            return fallback;
        }
        return value == 0 ? 0 : 1;
    }
}
