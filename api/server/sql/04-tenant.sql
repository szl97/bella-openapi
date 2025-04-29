SET NAMES utf8mb4;

CREATE TABLE `tenant`
(
    `id`                bigint unsigned                                               NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_code`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '租户编码',
    `tenant_name`       varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '租户名称',
    `tenant_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '租户描述',
    `status`            tinyint                                                       NOT NULL DEFAULT '0' COMMENT '删除状态(0未删除，-1已删除)',
    `ctime`             datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `mtime`             datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后一次更新时间',
    `owner_uid`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '租户拥有人系统号',
    `cuid`              bigint                                                        NOT NULL DEFAULT '0' COMMENT '租户创建人系统号',
    `muid`              bigint                                                        NOT NULL DEFAULT '0' COMMENT '租户最后一次更新人系统号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_idx_tenant_code` (`tenant_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='租户表';

-- Add tenant_code column to space table
ALTER TABLE `space` ADD COLUMN `tenant_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '租户编码' AFTER `space_code';
CREATE INDEX `idx_space_tenant_code` ON `space` (`tenant_code`);