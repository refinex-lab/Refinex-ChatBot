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
INSERT IGNORE INTO sys_user (id, username, mobile_cipher, mobile_index, email_cipher, email_index, password, nickname, sex, status, create_time)
VALUES (1, 'refinex', 'w55HcqYTCxm45jAawiGEyNpxXz5XI/yOI0MVOIPyzjOPIrvogEtL', 'c57eda2d10226c4de1015d122f10c95e761b1ad81f38c7cc40a662d727d3c758', 'cwW6TXH0PJKlTzSv3Xk0H3DXpldTS6fIZPRil5E41TZbC2nslFIFZyc9Lg==', '07412cb2ee0724653950d1899676790a89cd16d96f7b9f2ac01e44eaf4fd548f', '$2a$12$7gJV4RjuaVj3F9jo//nP6OzzUtL0mZIWcmhe4zrEhXQ5RJFPAemBq', '超级管理员', 'MALE', 1, NOW());

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


-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;