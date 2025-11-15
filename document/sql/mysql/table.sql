-- 创建数据库
CREATE DATABASE IF NOT EXISTS refinex_chatbot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 选择数据库
USE refinex_chatbot;

-- 设置编码
SET NAMES utf8mb4;

-- 关闭外键检查，避免创建表时出现外键约束错误
SET FOREIGN_KEY_CHECKS = 0;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user
(
    id              BIGINT                             NOT NULL COMMENT '主键ID' PRIMARY KEY,
    username        VARCHAR(50)                        NOT NULL COMMENT '登录用户名',
    mobile_cipher   VARCHAR(500)                       NULL COMMENT 'AES-GCM 加密后的手机号',
    mobile_index    VARCHAR(100)                       NULL COMMENT 'HMAC(手机号) 固定长度哈希值,用于 WHERE 查询匹配',
    email_cipher    VARCHAR(500)                       NULL COMMENT 'AES-GCM 加密后的邮箱',
    email_index     VARCHAR(100)                       NULL COMMENT 'HMAC(邮箱) 固定长度哈希值,用于 WHERE 查询匹配',
    password        VARCHAR(100)                       NOT NULL COMMENT 'BCrypt加密后的密码哈希',
    nickname        VARCHAR(50)                        NULL COMMENT '用户昵称',
    sex             VARCHAR(10)                        NULL COMMENT '性别:MALE,FEMALE,OTHER',
    avatar          VARCHAR(500)                       NULL COMMENT '头像URL',
    account_status  TINYINT  DEFAULT 1                 NOT NULL COMMENT '账户状态:1正常,2冻结,3注销',
    last_login_time DATETIME                           NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50)                        NULL COMMENT '最后登录IP',
    create_by       BIGINT                             NULL COMMENT '创建人ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by       BIGINT                             NULL COMMENT '更新人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by       BIGINT                             NULL COMMENT '删除人ID',
    delete_time     DATETIME                           NULL COMMENT '删除时间',
    remark          VARCHAR(500)                       NULL COMMENT '备注说明',
    status          TINYINT  DEFAULT 1                 NOT NULL COMMENT '启用状态:1启用,0停用',
    CONSTRAINT uni_username UNIQUE (username)
) COMMENT '用户表';

-- 初始化超级管理员
INSERT IGNORE INTO sys_user (id, username, mobile_cipher, mobile_index, email_cipher, email_index, password, nickname,
                             sex, status, create_time)
VALUES (1, 'refinex', 'w55HcqYTCxm45jAawiGEyNpxXz5XI/yOI0MVOIPyzjOPIrvogEtL',
        'c57eda2d10226c4de1015d122f10c95e761b1ad81f38c7cc40a662d727d3c758',
        'cwW6TXH0PJKlTzSv3Xk0H3DXpldTS6fIZPRil5E41TZbC2nslFIFZyc9Lg==',
        '07412cb2ee0724653950d1899676790a89cd16d96f7b9f2ac01e44eaf4fd548f',
        '$2a$12$7gJV4RjuaVj3F9jo//nP6OzzUtL0mZIWcmhe4zrEhXQ5RJFPAemBq', '超级管理员', 'MALE', 1, NOW());

-- 系统角色表
CREATE TABLE IF NOT EXISTS sys_role
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    is_builtin  TINYINT  DEFAULT 0                 NOT NULL COMMENT '系统内置角色标识:0非系统内部角色,1系统内部角色',
    role_code   VARCHAR(50)                        NOT NULL COMMENT '角色编码,如ROLE_ADMIN,ROLE_USER,ROLE_VIP_MONTHLY',
    role_name   VARCHAR(50)                        NOT NULL COMMENT '角色名称,',
    create_by   BIGINT                             NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by   BIGINT                             NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除标记:0未删除,1已删除',
    delete_by   BIGINT                             NULL COMMENT '删除人ID',
    remark      VARCHAR(500)                       NULL COMMENT '备注说明',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态:1正常,0停用',
    CONSTRAINT uni_role_code UNIQUE (role_code) COMMENT '角色编码唯一索引'
) COMMENT '角色表-定义系统中的所有角色';

-- 内置角色：超级管理员、普通用户、系统运维（示例）
INSERT IGNORE INTO sys_role (id, is_builtin, role_code, role_name, create_by, create_time, status)
VALUES (1, 1, 'ROLE_ADMIN', '超级管理员', NULL, NOW(), 1),
       (2, 1, 'ROLE_USER', '普通用户', NULL, NOW(), 1),
       (3, 1, 'ROLE_OPERATOR', '系统运维', NULL, NOW(), 1);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    user_id     BIGINT                             NOT NULL COMMENT '用户ID',
    role_id     BIGINT                             NOT NULL COMMENT '角色ID',
    create_by   BIGINT                             NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT uni_user_role UNIQUE (user_id, role_id) COMMENT '用户角色唯一索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    INDEX idx_role_id (role_id) COMMENT '角色ID索引'
) COMMENT '用户角色关联表-用户与角色多对多关系';

-- 为超级管理员(1)授权
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_by, create_time)
VALUES (1, 1, NULL, NOW());

-- 系统菜单表
CREATE TABLE IF NOT EXISTS sys_menu
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id   BIGINT                             NULL COMMENT '父级菜单ID，根节点为0',
    menu_name   VARCHAR(100)                       NOT NULL COMMENT '菜单名称',
    menu_code   VARCHAR(100)                       NOT NULL COMMENT '菜单编码(如 system:user:list)',
    menu_type   VARCHAR(20)                        NOT NULL COMMENT '菜单类型: DIR(目录) / MENU(菜单) / BUTTON(按钮)',
    route_path  VARCHAR(255)                       NULL COMMENT '前端路由路径',
    component   VARCHAR(255)                       NULL COMMENT '前端组件路径',
    icon        VARCHAR(100)                       NULL COMMENT '菜单图标',
    visible     TINYINT  DEFAULT 1                 NOT NULL COMMENT '是否显示:1显示,0隐藏',
    sort        INT      DEFAULT 0                 NOT NULL COMMENT '排序',
    remark      VARCHAR(500)                       NULL COMMENT '备注说明',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态:1正常,0禁用',
    create_by   BIGINT                             NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by   BIGINT                             NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    CONSTRAINT uni_menu_code UNIQUE (menu_code)
) COMMENT '系统菜单表-定义前端可访问的菜单和资源';

-- 系统菜单操作表
CREATE TABLE IF NOT EXISTS sys_menu_op
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    menu_id     BIGINT                             NOT NULL COMMENT '所属菜单ID',
    op_code     VARCHAR(100)                       NOT NULL COMMENT '操作编码(如 add、edit、delete、export)',
    op_name     VARCHAR(100)                       NOT NULL COMMENT '操作名称(如 新增、编辑、删除)',
    permission  VARCHAR(255)                       NULL COMMENT '操作权限标识(如 system:user:add)',
    sort        INT      DEFAULT 0                 NOT NULL COMMENT '排序',
    remark      VARCHAR(500)                       NULL COMMENT '备注说明',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态:1启用,0禁用',
    create_by   BIGINT                             NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by   BIGINT                             NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    CONSTRAINT fk_menu_op_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE,
    CONSTRAINT uni_menu_op_code UNIQUE (menu_id, op_code)
) COMMENT '系统菜单操作表-定义每个菜单下的具体操作';

-- 角色菜单表
CREATE TABLE IF NOT EXISTS sys_menu_role
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id     BIGINT                             NOT NULL COMMENT '角色ID',
    menu_id     BIGINT                             NOT NULL COMMENT '菜单ID',
    create_by   BIGINT                             NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE,
    CONSTRAINT uni_role_menu UNIQUE (role_id, menu_id)
) COMMENT '角色菜单关联表-定义角色可访问的菜单';

-- 角色菜单操作表
CREATE TABLE IF NOT EXISTS sys_menu_role_op
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id     BIGINT                             NOT NULL COMMENT '角色ID',
    menu_op_id  BIGINT                             NOT NULL COMMENT '菜单操作ID',
    create_by   BIGINT                             NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT fk_role_op_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_op_menu_op FOREIGN KEY (menu_op_id) REFERENCES sys_menu_op (id) ON DELETE CASCADE,
    CONSTRAINT uni_role_menu_op UNIQUE (role_id, menu_op_id)
) COMMENT '角色菜单操作关联表-定义角色可执行的具体操作';

-- 操作定义表
CREATE TABLE IF NOT EXISTS sys_op
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    op_code     VARCHAR(50)                        NOT NULL COMMENT '操作编码(如 ADD, EDIT, DELETE, READ, EXPORT)',
    op_name     VARCHAR(100)                       NOT NULL COMMENT '操作名称(如 新增, 编辑, 删除, 查看, 导出)',
    description VARCHAR(255)                       NULL COMMENT '操作描述',
    sort        INT      DEFAULT 0                 NOT NULL COMMENT '排序',
    builtin     TINYINT  DEFAULT 1                 NOT NULL COMMENT '是否系统内置:1是,0否',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态:1启用,0停用',
    create_by   BIGINT                             NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by   BIGINT                             NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    CONSTRAINT uni_op_code UNIQUE (op_code)
) COMMENT '系统操作定义表-统一定义系统中的标准操作';

INSERT IGNORE INTO sys_op (op_code, op_name, description, sort, builtin)
VALUES ('ADD', '新增', '创建新记录或数据项', 1, 1),
       ('EDIT', '编辑', '修改已有数据', 2, 1),
       ('DELETE', '删除', '逻辑或物理删除数据', 3, 1),
       ('READ', '查看', '查询或查看详细信息', 4, 1),
       ('EXPORT', '导出', '将数据导出为文件', 5, 1),
       ('IMPORT', '导入', '批量导入外部数据', 6, 1),
       ('ENABLE', '启用', '激活或启用某项功能', 7, 1),
       ('DISABLE', '禁用', '停用某项功能或账号', 8, 1),
       ('AUDIT', '审核', '进行审批或审核操作', 9, 1),
       ('DOWNLOAD', '下载', '下载文件或附件', 10, 1);

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id        BIGINT                             NULL COMMENT '用户ID,匿名登录失败时可为空',
    username       VARCHAR(50)                        NULL COMMENT '用户名',
    login_identity VARCHAR(255)                       NOT NULL COMMENT '登录标识(如邮箱)',
    status         TINYINT  DEFAULT 1                 NOT NULL COMMENT '登录状态:1成功,0失败',
    message        VARCHAR(500)                       NULL COMMENT '登录结果描述',
    login_ip       VARCHAR(64)                        NULL COMMENT '登录IP',
    login_location VARCHAR(255)                       NULL COMMENT '登录地(预留)',
    device_type    VARCHAR(32)                        NULL COMMENT '设备类型:PC/APP/H5',
    user_agent     VARCHAR(500)                       NULL COMMENT 'User-Agent',
    login_time     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '登录时间',
    create_by      BIGINT                             NULL COMMENT '创建人(通常为空)',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by      BIGINT                             NULL COMMENT '更新人(通常为空)',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_login_user (user_id),
    INDEX idx_login_time (login_time)
) COMMENT '用户登录日志表-记录每次登录行为';

-- 请求日志表
CREATE TABLE IF NOT EXISTS sys_request_log
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    service_name  VARCHAR(100)                        NOT NULL COMMENT '服务名称',
    title         VARCHAR(128)                        NOT NULL COMMENT '日志标题',
    biz_type      VARCHAR(32)   DEFAULT 'OTHER'       NOT NULL COMMENT '业务类型',
    description   VARCHAR(255)                       NULL COMMENT '业务描述',
    request_uri   VARCHAR(255)                        NOT NULL COMMENT '请求 URI',
    http_method   VARCHAR(16)                         NOT NULL COMMENT 'HTTP 方法',
    client_ip     VARCHAR(64)                        NULL COMMENT '客户端 IP',
    user_agent    VARCHAR(255)                       NULL COMMENT 'User-Agent',
    data_sign     VARCHAR(64)                         NOT NULL COMMENT 'DataSign',
    trace_id      VARCHAR(64)                         NOT NULL COMMENT 'TraceId',
    http_status   INT                                NULL COMMENT '响应状态码',
    success       TINYINT       DEFAULT 1            NOT NULL COMMENT '是否成功:1成功,0失败',
    user_id       BIGINT                            NULL COMMENT '用户ID',
    username      VARCHAR(64)                       NULL COMMENT '用户名',
    controller    VARCHAR(128)                      NULL COMMENT 'Controller 类名',
    method_name   VARCHAR(128)                      NULL COMMENT '方法名',
    request_body  MEDIUMTEXT                        NULL COMMENT '请求体',
    response_body MEDIUMTEXT                        NULL COMMENT '响应体',
    error_message VARCHAR(1000)                     NULL COMMENT '错误信息',
    duration_ms   BIGINT                             NOT NULL COMMENT '耗时(毫秒)',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间'
) COMMENT '请求日志表';

CREATE INDEX idx_sys_request_log_trace_id ON sys_request_log (trace_id);
CREATE INDEX idx_sys_request_log_data_sign ON sys_request_log (data_sign);
CREATE INDEX idx_sys_request_log_created ON sys_request_log (create_time);

-- ==========================
-- AI 与 RAG 相关表（Spring AI 接入）
-- ==========================-=

-- 模型供应商表
CREATE TABLE IF NOT EXISTS ai_provider
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    provider_code    VARCHAR(50)                        NOT NULL COMMENT '供应商编码: openai, azure, ollama, bedrock, qianwen, moonshot 等',
    provider_name    VARCHAR(100)                       NOT NULL COMMENT '供应商名称',
    provider_type    VARCHAR(30)                        NOT NULL COMMENT '供应商类型: public/private/self_hosted',
    base_url         VARCHAR(255)                       NULL COMMENT '基础 URL',
    api_key_cipher   VARCHAR(1024)                      NULL COMMENT 'API Key 密文(AES-GCM)',
    api_key_index    VARCHAR(128)                       NULL COMMENT 'API Key 索引(如KMS别名/HMAC)',
    rate_limit_qpm   INT                                NULL COMMENT '限流: QPM',
    status           TINYINT       DEFAULT 1            NOT NULL COMMENT '状态:1启用,0停用',
    create_by        BIGINT                              NULL COMMENT '创建人ID',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by        BIGINT                              NULL COMMENT '更新人ID',
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT       DEFAULT 0            NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by        BIGINT                              NULL COMMENT '删除人ID',
    delete_time      DATETIME                            NULL COMMENT '删除时间',
    remark           VARCHAR(500)                        NULL COMMENT '备注',
    CONSTRAINT uni_ai_provider_code UNIQUE (provider_code)
) COMMENT 'AI 模型供应商';

-- 模型表（涵盖 Chat/Embeddings/Image/Audio/Rerank/MCP 等）
CREATE TABLE IF NOT EXISTS ai_model
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    provider_id             BIGINT                             NOT NULL COMMENT '供应商ID(逻辑关联 ai_provider.id)',
    model_key               VARCHAR(100)                       NOT NULL COMMENT '供应商模型标识:如 gpt-4o-mini,text-embedding-3-large',
    model_name              VARCHAR(100)                       NULL COMMENT '模型名称',
    model_type              VARCHAR(30)                        NOT NULL COMMENT '模型类型: CHAT/EMBEDDING/IMAGE/AUDIO/RERANK/MCP',
    api_variant             VARCHAR(30)                        NULL COMMENT 'API 形态: openai/vertex/bedrock/azure/ollama 等',
    region                  VARCHAR(50)                        NULL COMMENT '区域(如 azure/bedrock 的 region)',
    context_window_tokens   INT                                NULL COMMENT '上下文窗口大小',
    max_output_tokens       INT                                NULL COMMENT '最大输出 tokens',
    price_input_per_1k      DECIMAL(18,8)                      NULL COMMENT '输入/1K tokens 价格',
    price_output_per_1k     DECIMAL(18,8)                      NULL COMMENT '输出/1K tokens 价格',
    currency                VARCHAR(16)  DEFAULT 'USD'         NOT NULL COMMENT '币种',
    support_tool_call       TINYINT       DEFAULT 0            NOT NULL COMMENT '是否支持工具调用',
    support_vision          TINYINT       DEFAULT 0            NOT NULL COMMENT '是否支持图像多模态',
    support_audio_in        TINYINT       DEFAULT 0            NOT NULL COMMENT '是否支持音频输入',
    support_audio_out       TINYINT       DEFAULT 0            NOT NULL COMMENT '是否支持音频输出',
    support_structured_out  TINYINT       DEFAULT 0            NOT NULL COMMENT '是否支持结构化输出',
    status                  TINYINT       DEFAULT 1            NOT NULL COMMENT '状态:1启用,0停用',
    create_by               BIGINT                              NULL COMMENT '创建人ID',
    create_time             DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by               BIGINT                              NULL COMMENT '更新人ID',
    update_time             DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 TINYINT       DEFAULT 0            NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by               BIGINT                              NULL COMMENT '删除人ID',
    delete_time             DATETIME                            NULL COMMENT '删除时间',
    remark                  VARCHAR(500)                        NULL COMMENT '备注',
    CONSTRAINT uni_ai_model UNIQUE (provider_id, model_key)
) COMMENT 'AI 模型';
CREATE INDEX idx_ai_model_provider ON ai_model (provider_id);
CREATE INDEX idx_ai_model_type ON ai_model (model_type);

-- 工具表（函数调用/MCP 工具/RAG 查询等）
CREATE TABLE IF NOT EXISTS ai_tool
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tool_code      VARCHAR(50)                        NOT NULL COMMENT '工具编码，唯一',
    tool_name      VARCHAR(100)                       NOT NULL COMMENT '工具名称',
    tool_type      VARCHAR(30)                        NOT NULL COMMENT '工具类型: FUNCTION/HTTP/MCP_TOOL/RAG_QUERY/SCRIPT/SYSTEM',
    impl_bean      VARCHAR(128)                       NULL COMMENT '实现 Bean 名或类名(用于 Spring 调用)',
    endpoint       VARCHAR(255)                       NULL COMMENT 'HTTP/Shell 等端点(可选)',
    timeout_ms     INT                                NULL COMMENT '超时时间(ms)',
    input_schema   JSON                               NULL COMMENT '输入 JSON-Schema',
    output_schema  JSON                               NULL COMMENT '输出 JSON-Schema',
    mcp_server_id  BIGINT                             NULL COMMENT '关联的 MCP Server(逻辑关联 ai_mcp_server.id)',
    status         TINYINT       DEFAULT 1            NOT NULL COMMENT '状态:1启用,0停用',
    create_by      BIGINT                              NULL COMMENT '创建人ID',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by      BIGINT                              NULL COMMENT '更新人ID',
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT       DEFAULT 0            NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by      BIGINT                              NULL COMMENT '删除人ID',
    delete_time    DATETIME                            NULL COMMENT '删除时间',
    remark         VARCHAR(500)                        NULL COMMENT '备注',
    CONSTRAINT uni_ai_tool_code UNIQUE (tool_code)
) COMMENT 'AI 工具定义';
CREATE INDEX idx_ai_tool_type ON ai_tool (tool_type);
CREATE INDEX idx_ai_tool_mcp ON ai_tool (mcp_server_id);

-- MCP Server 表
CREATE TABLE IF NOT EXISTS ai_mcp_server
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    server_code     VARCHAR(50)                        NOT NULL COMMENT 'MCP Server 编码',
    server_name     VARCHAR(100)                       NOT NULL COMMENT 'MCP Server 名称',
    transport_type  VARCHAR(30)                        NOT NULL COMMENT '传输: stdio/sse/ws/http',
    entry_command   VARCHAR(500)                       NULL COMMENT '启动命令或可执行路径(自托管)',
    endpoint_url    VARCHAR(255)                       NULL COMMENT '网络端点(远程)',
    manifest_url    VARCHAR(255)                       NULL COMMENT '清单/能力发现 URL',
    auth_type       VARCHAR(30)                        NULL COMMENT '鉴权类型: NONE/BEARER/BASIC',
    auth_secret_cipher VARCHAR(1024)                   NULL COMMENT '鉴权密钥密文(AES-GCM)',
    auth_secret_index  VARCHAR(128)                    NULL COMMENT '鉴权密钥索引/别名',
    tools_filter    VARCHAR(255)                       NULL COMMENT '工具白名单(逗号分隔)，为空表示全部可用',
    status          TINYINT       DEFAULT 1            NOT NULL COMMENT '状态:1启用,0停用',
    create_by       BIGINT                              NULL COMMENT '创建人ID',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by       BIGINT                              NULL COMMENT '更新人ID',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       DEFAULT 0            NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by       BIGINT                              NULL COMMENT '删除人ID',
    delete_time     DATETIME                            NULL COMMENT '删除时间',
    remark          VARCHAR(500)                        NULL COMMENT '备注',
    CONSTRAINT uni_ai_mcp_server_code UNIQUE (server_code)
) COMMENT 'MCP Server 定义';

-- 提示词主表
CREATE TABLE IF NOT EXISTS ai_prompt
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    prompt_code      VARCHAR(100)                       NOT NULL COMMENT '提示词编码，唯一',
    prompt_name      VARCHAR(100)                       NOT NULL COMMENT '提示词名称',
    category         VARCHAR(50)                        NULL COMMENT '分类: general/rag/agent/test 等',
    description      VARCHAR(500)                       NULL COMMENT '说明',
    template_format  VARCHAR(30)  DEFAULT 'SPRING'      NOT NULL COMMENT '模板格式: SPRING/MUSTACHE/STRING/LITERAL',
    role             VARCHAR(20)                         NULL COMMENT '默认角色: system/user/assistant/tool',
    template         MEDIUMTEXT                          NOT NULL COMMENT '模板正文',
    variables        JSON                                NULL COMMENT '变量示例/默认值(JSON)',
    examples         JSON                                NULL COMMENT 'Few-Shot 示例(JSON)',
    hash_sha256      VARCHAR(64)                         NULL COMMENT '内容摘要(用于防重)',
    input_schema     JSON                                NULL COMMENT '输入参数 JSON-Schema',
    status           TINYINT       DEFAULT 1             NOT NULL COMMENT '状态:1启用,0停用',
    create_by        BIGINT                               NULL COMMENT '创建人ID',
    create_time      DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by        BIGINT                               NULL COMMENT '更新人ID',
    update_time      DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT        DEFAULT 0             NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by        BIGINT                               NULL COMMENT '删除人ID',
    delete_time      DATETIME                             NULL COMMENT '删除时间',
    remark           VARCHAR(500)                         NULL COMMENT '备注',
    CONSTRAINT uni_ai_prompt_code UNIQUE (prompt_code)
) COMMENT '提示词主表';
CREATE INDEX idx_ai_prompt_category ON ai_prompt (category);

-- 结构化输出 Schema 表
CREATE TABLE IF NOT EXISTS ai_schema
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    schema_code   VARCHAR(100)                       NOT NULL COMMENT 'Schema 编码，唯一',
    schema_name   VARCHAR(100)                       NOT NULL COMMENT 'Schema 名称',
    schema_type   VARCHAR(30)                        NOT NULL COMMENT 'Schema 类型: JSON_SCHEMA/PROTO/XML/YAML',
    schema_json   JSON                               NOT NULL COMMENT 'Schema JSON 定义',
    version       INT              DEFAULT 1         NOT NULL COMMENT '版本号',
    strict_mode   TINYINT          DEFAULT 1         NOT NULL COMMENT '是否严格校验:1是,0否',
    status        TINYINT          DEFAULT 1         NOT NULL COMMENT '状态:1启用,0停用',
    create_by     BIGINT                               NULL COMMENT '创建人ID',
    create_time   DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by     BIGINT                               NULL COMMENT '更新人ID',
    update_time   DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT          DEFAULT 0          NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by     BIGINT                               NULL COMMENT '删除人ID',
    delete_time   DATETIME                             NULL COMMENT '删除时间',
    remark        VARCHAR(500)                         NULL COMMENT '备注',
    CONSTRAINT uni_ai_schema_code UNIQUE (schema_code)
) COMMENT '结构化输出 Schema 定义';

-- Advisors 定义表
CREATE TABLE IF NOT EXISTS ai_advisor
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    advisor_code VARCHAR(100)                       NOT NULL COMMENT 'Advisor 编码，唯一',
    advisor_name VARCHAR(100)                       NOT NULL COMMENT 'Advisor 名称',
    advisor_type VARCHAR(50)                        NOT NULL COMMENT '类型: MODERATION/RETRY/GUARDRAIL/VALIDATOR/ROUTER/CONTEXT_ENRICH 等',
    sort         INT              DEFAULT 0         NOT NULL COMMENT '链路顺序 越小越靠前',
    status       TINYINT          DEFAULT 1         NOT NULL COMMENT '状态:1启用,0停用',
    create_by    BIGINT                               NULL COMMENT '创建人ID',
    create_time  DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by    BIGINT                               NULL COMMENT '更新人ID',
    update_time  DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT          DEFAULT 0          NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by    BIGINT                               NULL COMMENT '删除人ID',
    delete_time  DATETIME                             NULL COMMENT '删除时间',
    remark       VARCHAR(500)                         NULL COMMENT '备注',
    CONSTRAINT uni_ai_advisor_code UNIQUE (advisor_code)
) COMMENT 'Advisors 定义';
CREATE INDEX idx_ai_advisor_type ON ai_advisor (advisor_type);

-- Agent/助手定义表（聚合模型/提示词/工具/Advisors/RAG 配置）
CREATE TABLE IF NOT EXISTS ai_agent
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agent_code         VARCHAR(100)                       NOT NULL COMMENT 'Agent 编码，唯一',
    agent_name         VARCHAR(100)                       NOT NULL COMMENT 'Agent 名称',
    description        VARCHAR(500)                       NULL COMMENT '说明',
    model_id           BIGINT                             NULL COMMENT '默认模型ID(逻辑关联 ai_model.id)',
    prompt_id          BIGINT                             NULL COMMENT '提示词ID(逻辑关联 ai_prompt.id)',
    output_schema_id   BIGINT                             NULL COMMENT '结构化输出 Schema(逻辑关联 ai_schema.id)',
    rag_kb_id          BIGINT                             NULL COMMENT '默认知识库ID(逻辑关联 kb_base.id)',
    temperature        DECIMAL(4,2)                       NULL COMMENT '温度值',
    top_p              DECIMAL(4,2)                       NULL COMMENT 'TopP',
    presence_penalty   DECIMAL(4,2)                       NULL COMMENT 'Presence Penalty',
    frequency_penalty  DECIMAL(4,2)                       NULL COMMENT 'Frequency Penalty',
    max_tokens         INT                                NULL COMMENT '最大生成 tokens',
    stop_sequences     JSON                               NULL COMMENT '停止词',
    tool_choice        VARCHAR(30)                        NULL COMMENT '工具选择策略: auto/none/required/指定tool',
    status             TINYINT          DEFAULT 1         NOT NULL COMMENT '状态:1启用,0停用',
    create_by          BIGINT                               NULL COMMENT '创建人ID',
    create_time        DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by          BIGINT                               NULL COMMENT '更新人ID',
    update_time        DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT          DEFAULT 0          NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by          BIGINT                               NULL COMMENT '删除人ID',
    delete_time        DATETIME                             NULL COMMENT '删除时间',
    remark             VARCHAR(500)                         NULL COMMENT '备注',
    CONSTRAINT uni_ai_agent_code UNIQUE (agent_code)
) COMMENT 'Agent/助手定义';
CREATE INDEX idx_ai_agent_model ON ai_agent (model_id);
CREATE INDEX idx_ai_agent_prompt ON ai_agent (prompt_id);
CREATE INDEX idx_ai_agent_kb ON ai_agent (rag_kb_id);

-- Agent 工具关联表
CREATE TABLE IF NOT EXISTS ai_agent_tool
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agent_id  BIGINT                             NOT NULL COMMENT 'Agent ID(逻辑关联 ai_agent.id)',
    tool_id   BIGINT                             NOT NULL COMMENT 'Tool ID(逻辑关联 ai_tool.id)',
    sort      INT              DEFAULT 0         NOT NULL COMMENT '顺序',
    create_by BIGINT                              NULL COMMENT '创建人ID',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT uni_ai_agent_tool UNIQUE (agent_id, tool_id)
) COMMENT 'Agent 与工具关联';
CREATE INDEX idx_ai_agent_tool_agent ON ai_agent_tool (agent_id);
CREATE INDEX idx_ai_agent_tool_tool ON ai_agent_tool (tool_id);

-- Agent Advisor 关联表
CREATE TABLE IF NOT EXISTS ai_agent_advisor
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agent_id    BIGINT                             NOT NULL COMMENT 'Agent ID(逻辑关联 ai_agent.id)',
    advisor_id  BIGINT                             NOT NULL COMMENT 'Advisor ID(逻辑关联 ai_advisor.id)',
    sort        INT              DEFAULT 0         NOT NULL COMMENT '顺序',
    create_by   BIGINT                              NULL COMMENT '创建人ID',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT uni_ai_agent_advisor UNIQUE (agent_id, advisor_id)
) COMMENT 'Agent 与 Advisor 关联';
CREATE INDEX idx_ai_agent_adv_agent ON ai_agent_advisor (agent_id);
CREATE INDEX idx_ai_agent_adv_adv ON ai_agent_advisor (advisor_id);

-- 聊天会话表（聊天窗口）
CREATE TABLE IF NOT EXISTS chat_session
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_code       VARCHAR(64)                        NULL COMMENT '会话编码(可用于分享/外部引用)',
    user_id            BIGINT                             NOT NULL COMMENT '用户ID',
    agent_id           BIGINT                             NULL COMMENT '默认 Agent(逻辑关联 ai_agent.id)',
    title              VARCHAR(200)                       NULL COMMENT '标题',
    summary            VARCHAR(500)                       NULL COMMENT '摘要(用于历史显示)',
    pinned             TINYINT          DEFAULT 0         NOT NULL COMMENT '是否置顶:1是,0否',
    archived           TINYINT          DEFAULT 0         NOT NULL COMMENT '是否归档:1是,0否',
    message_count      INT              DEFAULT 0         NOT NULL COMMENT '消息条数',
    token_count        INT              DEFAULT 0         NOT NULL COMMENT '累计 tokens',
    last_message_time  DATETIME                            NULL COMMENT '最后消息时间',
    status             TINYINT          DEFAULT 1          NOT NULL COMMENT '状态:1启用,0停用',
    create_by          BIGINT                                NULL COMMENT '创建人ID',
    create_time        DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by          BIGINT                                NULL COMMENT '更新人ID',
    update_time        DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT          DEFAULT 0           NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by          BIGINT                                NULL COMMENT '删除人ID',
    delete_time        DATETIME                              NULL COMMENT '删除时间',
    remark             VARCHAR(500)                          NULL COMMENT '备注',
    CONSTRAINT uni_chat_session_code UNIQUE (session_code)
) COMMENT '聊天会话(窗口)';
CREATE INDEX idx_chat_session_user ON chat_session (user_id);
CREATE INDEX idx_chat_session_agent ON chat_session (agent_id);
CREATE INDEX idx_chat_session_time ON chat_session (last_message_time);

-- 会话消息表（支持多模态、工具调用、结构化输出）
CREATE TABLE IF NOT EXISTS chat_message
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_id         BIGINT                             NOT NULL COMMENT '会话ID(逻辑关联 chat_session.id)',
    parent_message_id  BIGINT                              NULL COMMENT '父消息ID(用于树/线程)',
    role               VARCHAR(30)                         NOT NULL COMMENT '角色: system/user/assistant/tool',
    message_type       VARCHAR(30)   DEFAULT 'NORMAL'      NOT NULL COMMENT '消息类型: NORMAL/ERROR/TOOL_CALL/TOOL_RESULT/EVENT',
    content_text       LONGTEXT                            NULL COMMENT '文本内容',
    content_format     VARCHAR(20)   DEFAULT 'TEXT'        NOT NULL COMMENT '内容格式: TEXT/MARKDOWN/JSON',
    content_json       JSON                                 NULL COMMENT '结构化内容(JSON)',
    attachments_count  INT              DEFAULT 0          NOT NULL COMMENT '附件数量',
    tool_calls         JSON                                 NULL COMMENT '工具调用请求(JSON)',
    tool_results       JSON                                 NULL COMMENT '工具调用结果(JSON)',
    provider_id        BIGINT                               NULL COMMENT '供应商ID(逻辑关联 ai_provider.id)',
    model_id           BIGINT                               NULL COMMENT '模型ID(逻辑关联 ai_model.id)',
    finish_reason      VARCHAR(30)                          NULL COMMENT '结束原因: stop/length/tool_calls/other',
    input_tokens       INT                                  NULL COMMENT '提示 tokens',
    output_tokens      INT                                  NULL COMMENT '补全 tokens',
    latency_ms         BIGINT                               NULL COMMENT '耗时(ms)',
    cost               DECIMAL(18,8)                        NULL COMMENT '花费(转换成统一币种,如 USD)',
    currency           VARCHAR(16)   DEFAULT 'USD'          NOT NULL COMMENT '币种',
    error_code         VARCHAR(64)                          NULL COMMENT '错误码',
    error_message      VARCHAR(500)                         NULL COMMENT '错误信息',
    message_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '消息时间',
    status             TINYINT        DEFAULT 1             NOT NULL COMMENT '状态:1正常,0异常',
    create_by          BIGINT                                 NULL COMMENT '创建人ID',
    create_time        DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by          BIGINT                                 NULL COMMENT '更新人ID',
    update_time        DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT        DEFAULT 0              NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    remark             VARCHAR(500)                           NULL COMMENT '备注'
) COMMENT '会话消息';
CREATE INDEX idx_chat_msg_session ON chat_message (session_id);
CREATE INDEX idx_chat_msg_session_time ON chat_message (session_id, message_time);
CREATE INDEX idx_chat_msg_parent ON chat_message (parent_message_id);
CREATE INDEX idx_chat_msg_role ON chat_message (role);

-- 消息附件表（多模态资源）
CREATE TABLE IF NOT EXISTS chat_attachment
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    message_id       BIGINT                             NOT NULL COMMENT '消息ID(逻辑关联 chat_message.id)',
    file_id          BIGINT                              NULL COMMENT '关联系统文件ID(逻辑关联 sys_file.id)',
    file_name        VARCHAR(255)                       NULL COMMENT '文件名',
    uri              VARCHAR(500)                       NOT NULL COMMENT '存储URI(OSS/本地/HTTP等)',
    storage_provider VARCHAR(50)                        NULL COMMENT '存储提供方: local/minio/oss/s3/http',
    media_type       VARCHAR(30)                        NOT NULL COMMENT '媒体类型: IMAGE/AUDIO/VIDEO/DOCUMENT/OTHER',
    mime_type        VARCHAR(100)                       NULL COMMENT 'MIME 类型',
    size_bytes       BIGINT                             NULL COMMENT '大小(字节)',
    width            INT                                NULL COMMENT '图片宽',
    height           INT                                NULL COMMENT '图片高',
    duration_ms      BIGINT                             NULL COMMENT '音视频时长(ms)',
    transcript_text  MEDIUMTEXT                         NULL COMMENT '转写文本(可选)',
    kb_doc_id        BIGINT                             NULL COMMENT '关联知识文档ID(逻辑关联 kb_document.id)',
    kb_chunk_id      BIGINT                             NULL COMMENT '关联分片ID(逻辑关联 kb_chunk_meta.id)',
    external_vector_id VARCHAR(128)                     NULL COMMENT '外部向量ID(向量库)',
    metadata         JSON                               NULL COMMENT '扩展元数据',
    status           TINYINT        DEFAULT 1           NOT NULL COMMENT '状态:1正常,0禁用',
    create_by        BIGINT                               NULL COMMENT '创建人ID',
    create_time      DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by        BIGINT                               NULL COMMENT '更新人ID',
    update_time      DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT        DEFAULT 0            NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    remark           VARCHAR(500)                         NULL COMMENT '备注'
) COMMENT '聊天消息附件';
CREATE INDEX idx_chat_att_msg ON chat_attachment (message_id);
CREATE INDEX idx_chat_att_kbdoc ON chat_attachment (kb_doc_id);
CREATE INDEX idx_chat_att_file ON chat_attachment (file_id);

-- 统一使用/计费日志（也可用作非聊天调用，如 Embedding/Image/AUDIO 等）
CREATE TABLE IF NOT EXISTS ai_usage_log
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    request_id      VARCHAR(64)                        NULL COMMENT '请求ID/TraceId',
    user_id         BIGINT                             NULL COMMENT '用户ID',
    session_id      BIGINT                             NULL COMMENT '会话ID',
    provider_id     BIGINT                             NULL COMMENT '供应商ID',
    model_id        BIGINT                             NULL COMMENT '模型ID',
    model_key       VARCHAR(100)                       NULL COMMENT '模型Key',
    operation       VARCHAR(30)                        NOT NULL COMMENT '操作类型: CHAT/EMBEDDING/IMAGE/AUDIO/TOOL/RERANK',
    input_tokens    INT                                NULL COMMENT '输入 tokens',
    output_tokens   INT                                NULL COMMENT '输出 tokens',
    cost            DECIMAL(18,8)                      NULL COMMENT '花费(统一币种)',
    currency        VARCHAR(16)  DEFAULT 'USD'         NOT NULL COMMENT '币种',
    success         TINYINT       DEFAULT 1            NOT NULL COMMENT '是否成功:1成功,0失败',
    http_status     INT                                 NULL COMMENT 'HTTP 状态码(如有)',
    latency_ms      BIGINT                              NULL COMMENT '耗时(ms)',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    INDEX idx_ai_usage_req (request_id),
    INDEX idx_ai_usage_user (user_id),
    INDEX idx_ai_usage_session (session_id),
    INDEX idx_ai_usage_model (model_id),
    INDEX idx_ai_usage_op (operation),
    INDEX idx_ai_usage_time (create_time)
) COMMENT 'AI 调用使用/计费日志';

-- ==========================
-- 系统附件与存储配置（通用）
-- 支持本地/对象存储(DB/OSS/MinIO/S3等)，元信息与数据分离
-- ==========================

-- 存储配置表（支持多实例）
CREATE TABLE IF NOT EXISTS sys_storage_config
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    storage_code      VARCHAR(50)                        NOT NULL COMMENT '存储编码，唯一，如 local, s3_main, oss_public',
    storage_name      VARCHAR(100)                       NOT NULL COMMENT '存储名称',
    storage_type      VARCHAR(30)                        NOT NULL COMMENT '类型: LOCAL/DB/S3/OSS/MINIO/GCS/AZURE',
    endpoint          VARCHAR(255)                       NULL COMMENT 'Endpoint(如 https://s3.amazonaws.com 或内网域名)',
    region            VARCHAR(50)                        NULL COMMENT 'Region',
    bucket            VARCHAR(100)                       NULL COMMENT 'Bucket/容器 名称',
    base_path         VARCHAR(255)                       NULL COMMENT '基础路径/前缀(如 uploads/ )',
    base_url          VARCHAR(255)                       NULL COMMENT '对外访问基址(如 CDN 域名)',
    access_key_cipher VARCHAR(1024)                      NULL COMMENT 'AccessKey 密文(AES-GCM)',
    access_key_index  VARCHAR(128)                       NULL COMMENT 'AccessKey 索引/别名(HMAC/KMS别名)',
    secret_key_cipher VARCHAR(1024)                      NULL COMMENT 'SecretKey 密文(AES-GCM)',
    secret_key_index  VARCHAR(128)                       NULL COMMENT 'SecretKey 索引/别名',
    session_policy    JSON                               NULL COMMENT '会话策略/STS 配置(JSON)',
    is_default        TINYINT         DEFAULT 0          NOT NULL COMMENT '是否为默认存储:1是,0否',
    ext_config        JSON                               NULL COMMENT '其他扩展配置(JSON)，如签名算法、ACL、加速域名等',
    status            TINYINT         DEFAULT 1          NOT NULL COMMENT '状态:1启用,0停用',
    create_by         BIGINT                               NULL COMMENT '创建人ID',
    create_time       DATETIME        DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by         BIGINT                               NULL COMMENT '更新人ID',
    update_time       DATETIME        DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT         DEFAULT 0           NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    remark            VARCHAR(500)                         NULL COMMENT '备注',
    CONSTRAINT uni_sys_storage_code UNIQUE (storage_code)
) COMMENT '存储配置';
CREATE INDEX idx_sys_storage_type ON sys_storage_config (storage_type);
CREATE INDEX idx_sys_storage_default ON sys_storage_config (is_default);

-- 系统文件元信息（支持去重、哈希）
CREATE TABLE IF NOT EXISTS sys_file
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    storage_code       VARCHAR(50)                        NOT NULL COMMENT '存储编码(逻辑关联 sys_storage_config.storage_code)',
    file_key           VARCHAR(255)                       NULL COMMENT '对象键/路径(对于对象存储)',
    uri                VARCHAR(500)                       NULL COMMENT '可访问 URI(如 s3://bucket/key 或 https://...)',
    file_name          VARCHAR(255)                       NULL COMMENT '原始文件名',
    ext                VARCHAR(20)                        NULL COMMENT '扩展名',
    mime_type          VARCHAR(100)                       NULL COMMENT 'MIME 类型',
    size_bytes         BIGINT                             NULL COMMENT '文件大小',
    checksum_sha256    VARCHAR(64)                        NULL COMMENT 'SHA-256 校验和',
    width              INT                                 NULL COMMENT '图片宽(像素)',
    height             INT                                 NULL COMMENT '图片高(像素)',
    duration_ms        BIGINT                              NULL COMMENT '音视频时长(ms)',
    encrypt_algo       VARCHAR(30)                         NULL COMMENT '加密算法: NONE/AES256/KMS等',
    is_db_stored       TINYINT         DEFAULT 0           NOT NULL COMMENT '是否存储在数据库:1是,0否',
    biz_type           VARCHAR(50)                         NULL COMMENT '业务类型: CHAT_MSG/USER_AVATAR/DOC/KB/OTHER',
    biz_id             VARCHAR(100)                        NULL COMMENT '业务ID(字符串，兼容多种主键)',
    title              VARCHAR(255)                        NULL COMMENT '标题/说明',
    sort               INT              DEFAULT 0          NOT NULL COMMENT '排序',
    status             TINYINT        DEFAULT 1            NOT NULL COMMENT '状态:1正常,0禁用',
    create_by          BIGINT                                 NULL COMMENT '创建人ID',
    create_time        DATETIME        DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by          BIGINT                                 NULL COMMENT '更新人ID',
    update_time        DATETIME        DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT         DEFAULT 0             NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by          BIGINT                                 NULL COMMENT '删除人ID',
    delete_time        DATETIME                               NULL COMMENT '删除时间',
    remark             VARCHAR(500)                           NULL COMMENT '备注',
    CONSTRAINT uni_sys_file_dedup UNIQUE (storage_code, file_key, checksum_sha256)
) COMMENT '系统文件元信息';
CREATE INDEX idx_sys_file_storage ON sys_file (storage_code);
CREATE INDEX idx_sys_file_hash ON sys_file (checksum_sha256);
CREATE INDEX idx_sys_file_mime ON sys_file (mime_type);
CREATE INDEX idx_sys_file_biz ON sys_file (biz_type, biz_id);

-- 系统文件数据（仅当 is_db_stored=1 时使用）
CREATE TABLE IF NOT EXISTS sys_file_data
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    file_id     BIGINT                             NOT NULL COMMENT '文件ID(逻辑关联 sys_file.id)',
    data        LONGBLOB                           NOT NULL COMMENT '文件二进制数据',
    create_by   BIGINT                              NULL COMMENT '创建人ID',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by   BIGINT                              NULL COMMENT '更新人ID',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       DEFAULT 0             NOT NULL COMMENT '逻辑删除:0未删除,1已删除'
) COMMENT '系统文件数据(数据库存储)';
CREATE UNIQUE INDEX uni_sys_file_data_fid ON sys_file_data (file_id);

-- ==========================
-- RAG 知识库相关（不在 MySQL 存储向量，只做元数据与绑定）
-- ==========================

-- 知识库表
CREATE TABLE IF NOT EXISTS kb_base
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    kb_code                  VARCHAR(50)                        NOT NULL COMMENT '知识库编码，唯一',
    kb_name                  VARCHAR(100)                       NOT NULL COMMENT '知识库名称',
    description              VARCHAR(500)                       NULL COMMENT '描述',
    visibility               VARCHAR(20)   DEFAULT 'PRIVATE'    NOT NULL COMMENT '可见性: PRIVATE/TEAM/ORG/PUBLIC',
    owner_user_id            BIGINT                              NULL COMMENT '所有者用户ID',
    vector_store_type        VARCHAR(50)                        NOT NULL COMMENT '向量库类型: milvus/qdrant/pgvector/es/pinecone 等',
    vector_store_collection  VARCHAR(100)                       NULL COMMENT '向量库集合/命名空间',
    vector_config            JSON                               NULL COMMENT '向量库配置(JSON)',
    embed_model_id           BIGINT                              NULL COMMENT '默认Embedding模型ID(逻辑关联 ai_model.id)',
    embed_model_key          VARCHAR(100)                       NULL COMMENT '默认Embedding模型Key',
    dimension                INT                                 NULL COMMENT '维度(如需要)',
    metric                   VARCHAR(30)                        NULL COMMENT '度量方式: cosine/l2/ip',
    rag_strategy             VARCHAR(50)   DEFAULT 'VECTOR'     NOT NULL COMMENT '检索策略: VECTOR/BM25/HYBRID/MMR/RRF',
    default_top_k            INT           DEFAULT 5            NOT NULL COMMENT '默认TopK',
    doc_count                INT           DEFAULT 0            NOT NULL COMMENT '文档数',
    status                   TINYINT       DEFAULT 1            NOT NULL COMMENT '状态:1启用,0停用',
    create_by                BIGINT                               NULL COMMENT '创建人ID',
    create_time              DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by                BIGINT                               NULL COMMENT '更新人ID',
    update_time              DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                  TINYINT        DEFAULT 0            NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by                BIGINT                               NULL COMMENT '删除人ID',
    delete_time              DATETIME                             NULL COMMENT '删除时间',
    remark                   VARCHAR(500)                         NULL COMMENT '备注',
    CONSTRAINT uni_kb_code UNIQUE (kb_code)
) COMMENT 'RAG 知识库';
CREATE INDEX idx_kb_owner ON kb_base (owner_user_id);
CREATE INDEX idx_kb_visibility ON kb_base (visibility);

-- 知识库目录表（多级）
CREATE TABLE IF NOT EXISTS kb_catalog
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    kb_id        BIGINT                             NOT NULL COMMENT '知识库ID(逻辑关联 kb_base.id)',
    parent_id    BIGINT                              NULL COMMENT '父目录ID(根为空)',
    path         VARCHAR(500)                        NULL COMMENT '层级路径表达，如 /a/b',
    name         VARCHAR(200)                        NOT NULL COMMENT '目录名称',
    depth        INT              DEFAULT 0          NOT NULL COMMENT '层级深度，从0开始',
    sort         INT              DEFAULT 0          NOT NULL COMMENT '排序',
    doc_count    INT              DEFAULT 0          NOT NULL COMMENT '目录下文档数量(冗余)',
    status       TINYINT          DEFAULT 1          NOT NULL COMMENT '状态:1启用,0停用',
    create_by    BIGINT                               NULL COMMENT '创建人ID',
    create_time  DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by    BIGINT                               NULL COMMENT '更新人ID',
    update_time  DATETIME         DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT          DEFAULT 0           NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    remark       VARCHAR(500)                         NULL COMMENT '备注'
) COMMENT '知识库目录';
CREATE INDEX idx_kb_catalog_kb ON kb_catalog (kb_id);
CREATE INDEX idx_kb_catalog_parent ON kb_catalog (parent_id);

-- 知识文档表（不存储向量，仅存元数据与绑定）
CREATE TABLE IF NOT EXISTS kb_document
(
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    kb_id                      BIGINT                             NOT NULL COMMENT '知识库ID(逻辑关联 kb_base.id)',
    catalog_id                 BIGINT                              NULL COMMENT '目录ID(逻辑关联 kb_catalog.id)',
    title                      VARCHAR(255)                       NOT NULL COMMENT '标题',
    source_type                VARCHAR(30)                        NOT NULL COMMENT '来源类型: UPLOAD/URL/REPO/S3/CONFLUENCE/GIT 等',
    source_uri                 VARCHAR(500)                       NULL COMMENT '来源URI',
    language                   VARCHAR(20)                        NULL COMMENT '语种: zh/en/ja 等',
    author                     VARCHAR(100)                       NULL COMMENT '作者(可选)',
    file_name                  VARCHAR(255)                       NULL COMMENT '文件名(上传场景)',
    file_ext                   VARCHAR(20)                        NULL COMMENT '扩展名',
    mime_type                  VARCHAR(100)                       NULL COMMENT 'MIME 类型',
    size_bytes                 BIGINT                              NULL COMMENT '文件大小',
    checksum_sha256            VARCHAR(64)                        NULL COMMENT 'SHA-256 校验和',
    page_count                 INT                                  NULL COMMENT '页数(如PDF)',
    latest_version             INT            DEFAULT 1            NOT NULL COMMENT '最新版本号',
    index_latest_version       INT                                  NULL COMMENT '已索引最新版本号',
    external_doc_id            VARCHAR(128)                        NULL COMMENT '外部文档ID(第三方/存储/向量库绑定)',
    vector_store_collection    VARCHAR(100)                        NULL COMMENT '向量集合名(如按库/目录分集合)',
    status                     TINYINT        DEFAULT 1            NOT NULL COMMENT '状态:1正常,0禁用',
    create_by                  BIGINT                                 NULL COMMENT '创建人ID',
    create_time                DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by                  BIGINT                                 NULL COMMENT '更新人ID',
    update_time                DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                    TINYINT        DEFAULT 0              NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    delete_by                  BIGINT                                 NULL COMMENT '删除人ID',
    delete_time                DATETIME                               NULL COMMENT '删除时间',
    remark                     VARCHAR(500)                           NULL COMMENT '备注',
    CONSTRAINT uni_kb_doc_dedup UNIQUE (kb_id, source_uri, checksum_sha256)
) COMMENT '知识文档';
CREATE INDEX idx_kb_doc_kb ON kb_document (kb_id);
CREATE INDEX idx_kb_doc_catalog ON kb_document (catalog_id);

-- 知识文档历史/版本表
CREATE TABLE IF NOT EXISTS kb_document_version
(
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    doc_id                     BIGINT                             NOT NULL COMMENT '文档ID(逻辑关联 kb_document.id)',
    version                    INT                                NOT NULL COMMENT '版本号(从1开始)',
    title                      VARCHAR(255)                        NULL COMMENT '版本标题(可不同)',
    change_log                 VARCHAR(500)                        NULL COMMENT '变更说明',
    source_uri                 VARCHAR(500)                        NULL COMMENT '版本来源URI',
    file_name                  VARCHAR(255)                        NULL COMMENT '文件名',
    file_ext                   VARCHAR(20)                         NULL COMMENT '扩展名',
    mime_type                  VARCHAR(100)                        NULL COMMENT 'MIME 类型',
    size_bytes                 BIGINT                               NULL COMMENT '文件大小',
    checksum_sha256            VARCHAR(64)                          NULL COMMENT 'SHA-256 校验和',
    parse_status               VARCHAR(20)   DEFAULT 'DONE'         NOT NULL COMMENT '解析状态: PENDING/PARSING/DONE/FAILED',
    index_status               VARCHAR(20)   DEFAULT 'PENDING'      NOT NULL COMMENT '索引状态: PENDING/INDEXING/DONE/FAILED',
    chunk_count                INT                                  NULL COMMENT '切片数量',
    vector_count               INT                                  NULL COMMENT '向量条数(仅统计)',
    external_index_id          VARCHAR(128)                         NULL COMMENT '外部索引ID/任务ID',
    status                     TINYINT        DEFAULT 1             NOT NULL COMMENT '状态:1有效,0无效',
    create_by                  BIGINT                                  NULL COMMENT '创建人ID',
    create_time                DATETIME        DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by                  BIGINT                                  NULL COMMENT '更新人ID',
    update_time                DATETIME        DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                    TINYINT         DEFAULT 0             NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    remark                     VARCHAR(500)                           NULL COMMENT '备注',
    CONSTRAINT uni_kb_doc_ver UNIQUE (doc_id, version)
) COMMENT '知识文档版本';
CREATE INDEX idx_kb_doc_ver_doc ON kb_document_version (doc_id);
CREATE INDEX idx_kb_doc_ver_status ON kb_document_version (index_status);

-- 文档切片元数据（不存向量，只存元信息与外部向量ID）
CREATE TABLE IF NOT EXISTS kb_chunk_meta
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    doc_version_id       BIGINT                             NOT NULL COMMENT '文档版本ID(逻辑关联 kb_document_version.id)',
    chunk_index          INT                                NOT NULL COMMENT '切片序号(从0或1开始)',
    page_from            INT                                 NULL COMMENT '起始页(如有)',
    page_to              INT                                 NULL COMMENT '结束页(如有)',
    start_offset         INT                                 NULL COMMENT '起始偏移(字符)',
    end_offset           INT                                 NULL COMMENT '结束偏移(字符)',
    token_count          INT                                 NULL COMMENT '估算 tokens',
    content_preview      VARCHAR(2000)                       NULL COMMENT '内容预览(不存全文)',
    external_vector_id   VARCHAR(128)                        NULL COMMENT '外部向量ID',
    external_shard_id    VARCHAR(64)                         NULL COMMENT '外部分片/分区ID(可选)',
    metadata             JSON                                NULL COMMENT '元数据(JSON)，如标题/章节/标签',
    status               TINYINT        DEFAULT 1            NOT NULL COMMENT '状态:1有效,0无效',
    create_by            BIGINT                                NULL COMMENT '创建人ID',
    create_time          DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by            BIGINT                                NULL COMMENT '更新人ID',
    update_time          DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              TINYINT        DEFAULT 0             NOT NULL COMMENT '逻辑删除:0未删除,1已删除',
    remark               VARCHAR(500)                          NULL COMMENT '备注'
) COMMENT 'RAG 文档切片元数据';
CREATE INDEX idx_kb_chunk_doc_ver ON kb_chunk_meta (doc_version_id);
CREATE INDEX idx_kb_chunk_vec ON kb_chunk_meta (external_vector_id);

-- 知识入库/索引任务
CREATE TABLE IF NOT EXISTS kb_ingest_job
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    kb_id          BIGINT                             NULL COMMENT '知识库ID',
    doc_id         BIGINT                             NULL COMMENT '文档ID',
    job_type       VARCHAR(30)                        NOT NULL COMMENT '任务类型: INGEST/REINDEX/DELETE/REFRESH',
    status         VARCHAR(20)   DEFAULT 'PENDING'    NOT NULL COMMENT '状态: PENDING/RUNNING/DONE/FAILED',
    progress       INT           DEFAULT 0            NOT NULL COMMENT '进度百分比 0-100',
    error_message  VARCHAR(1000)                      NULL COMMENT '错误信息',
    started_time   DATETIME                            NULL COMMENT '开始时间',
    finished_time  DATETIME                            NULL COMMENT '完成时间',
    extra          JSON                                NULL COMMENT '扩展参数/上下文(JSON)',
    create_by      BIGINT                               NULL COMMENT '创建人ID',
    create_time    DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by      BIGINT                               NULL COMMENT '更新人ID',
    update_time    DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '知识入库/索引任务';
CREATE INDEX idx_kb_job_kb ON kb_ingest_job (kb_id);
CREATE INDEX idx_kb_job_doc ON kb_ingest_job (doc_id);
CREATE INDEX idx_kb_job_status ON kb_ingest_job (status);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
