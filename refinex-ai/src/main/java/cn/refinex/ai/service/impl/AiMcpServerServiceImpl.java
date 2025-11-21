package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerCreateRequestDTO;
import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerPageRequest;
import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerUpdateRequestDTO;
import cn.refinex.ai.controller.mcp.dto.response.AiMcpServerResponseDTO;
import cn.refinex.ai.converter.AiMcpServerConverter;
import cn.refinex.ai.entity.AiMcpServer;
import cn.refinex.ai.repository.AiMcpServerRepository;
import cn.refinex.ai.service.AiMcpServerService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * MCP Server 服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiMcpServerServiceImpl implements AiMcpServerService {

    private final AiMcpServerRepository repository;
    private final AiMcpServerConverter converter;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 分页查询 MCP Server
     *
     * @param request 查询请求
     * @return MCP Server 列表
     */
    @Override
    public PageResponse<AiMcpServerResponseDTO> page(AiMcpServerPageRequest request) {
        AiMcpServerPageRequest query = Objects.isNull(request) ? new AiMcpServerPageRequest() : request;
        Long userId = LoginHelper.getUserId();
        PageResponse<AiMcpServer> page = repository.page(
                trimToNull(query.getTransportType()), query.getStatus(),
                trimToNull(query.getKeyword()), query, userId
        );
        return page.map(converter::toResponse);
    }

    /**
     * 根据 ID 查询 MCP Server
     *
     * @param id 主键
     * @return MCP Server
     */
    @Override
    public Optional<AiMcpServerResponseDTO> findById(Long id) {
        return repository.findById(id, LoginHelper.getUserId()).map(converter::toResponse);
    }

    /**
     * 新增 MCP Server
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void create(AiMcpServerCreateRequestDTO request, Long operatorId) {
        String code = request.serverCode().trim();
        if (repository.findByCode(code, operatorId).isPresent()) {
            throw new BusinessException("MCP Server 编码已存在: " + code);
        }

        AiMcpServer entity = buildEntity(request, operatorId);
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
     * 更新 MCP Server
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void update(Long id, AiMcpServerUpdateRequestDTO request, Long operatorId) {
        AiMcpServer exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("MCP Server 不存在或已删除"));

        String newCode = request.serverCode().trim();
        if (!exist.getServerCode().equals(newCode) && repository.findByCode(newCode, operatorId).isPresent()) {
            throw new BusinessException("MCP Server 编码已存在: " + newCode);
        }

        AiMcpServer entity = buildEntity(new AiMcpServerCreateRequestDTO(
                request.serverCode(), request.serverName(), request.transportType(), request.entryCommand(),
                request.endpointUrl(), request.manifestUrl(), request.authType(), request.authSecretCipher(),
                request.authSecretIndex(), request.toolsFilter(), request.status(), request.remark()), operatorId
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
     * 更新 MCP Server 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        AiMcpServer exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("MCP Server 不存在或已删除"));

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
     * 删除 MCP Server
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiMcpServer exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("MCP Server 不存在或已删除"));

        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(exist.getId(), operatorId, jdbc);
            return null;
        });
    }

    /**
     * 构建 MCP Server 实体
     *
     * @param request    请求
     * @param operatorId 操作人 ID
     * @return MCP Server 实体
     */
    private AiMcpServer buildEntity(AiMcpServerCreateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        return AiMcpServer.builder()
                .serverCode(request.serverCode().trim())
                .serverName(request.serverName().trim())
                .transportType(request.transportType().trim())
                .entryCommand(trimToNull(request.entryCommand()))
                .endpointUrl(trimToNull(request.endpointUrl()))
                .manifestUrl(trimToNull(request.manifestUrl()))
                .authType(trimToNull(request.authType()))
                .authSecretCipher(trimToNull(request.authSecretCipher()))
                .authSecretIndex(trimToNull(request.authSecretIndex()))
                .toolsFilter(trimToNull(request.toolsFilter()))
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
     * 标准化状态标志
     *
     * @param value       值
     * @param defaultValue 默认值
     * @return 标准化后的状态标志
     */
    private int normalizeFlag(Integer value, Integer defaultValue) {
        int fallback = defaultValue == null ? 0 : defaultValue;
        if (value == null) {
            return fallback;
        }
        return value == 0 ? 0 : 1;
    }
}
