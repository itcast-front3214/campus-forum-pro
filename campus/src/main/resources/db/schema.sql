-- ============================================================
--  Campus Forum Database Schema  v2.0
--  字符集：utf8mb4（支持 emoji）
--  存储引擎：InnoDB（支持事务和外键）
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_forum
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_forum;

-- ============================================================
--  用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`   VARCHAR(50)  NOT NULL COMMENT '登录用户名（唯一）',
    `password`   VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
    `nickname`   VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '显示昵称',
    `email`      VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `avatar_url` VARCHAR(500)          DEFAULT NULL COMMENT '头像URL',
    `role`       VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT '角色：STUDENT / MODERATOR / ADMIN',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户表';

-- ============================================================
--  帖子表
-- ============================================================
CREATE TABLE IF NOT EXISTS `post`
(
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `user_id`       BIGINT        NOT NULL COMMENT '发帖用户ID',
    `title`         VARCHAR(200)  NOT NULL COMMENT '标题',
    `content`       LONGTEXT      NOT NULL COMMENT '正文内容',
    `type`          VARCHAR(20)   NOT NULL DEFAULT 'DISCUSSION' COMMENT '类型：DISCUSSION/QUESTION/NOTICE/SHARE',
    `audit_status`  VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '审核：PENDING/REVIEWING/APPROVED/REJECTED',
    `audit_remark`  VARCHAR(500)           DEFAULT NULL COMMENT '审核备注',
    `tags`          VARCHAR(500)           DEFAULT NULL COMMENT '标签，逗号分隔',
    `view_count`    INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count`    INT           NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT           NOT NULL DEFAULT 0 COMMENT '评论数',
    `is_pinned`     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否，1=是',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`),
    -- 热榜常用：按浏览量排序的帖子
    KEY `idx_view_count` (`view_count`),
    FULLTEXT KEY `ft_title` (`title`) WITH PARSER ngram,
    CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '帖子表';

-- ============================================================
--  评论表（支持二级嵌套）
-- ============================================================
CREATE TABLE IF NOT EXISTS `comment`
(
    `id`                BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id`           BIGINT   NOT NULL COMMENT '所属帖子ID',
    `user_id`           BIGINT   NOT NULL COMMENT '评论者ID',
    `parent_comment_id` BIGINT            DEFAULT NULL COMMENT '父评论ID，null=顶级评论',
    `content`           TEXT     NOT NULL COMMENT '评论内容',
    `like_count`        INT      NOT NULL DEFAULT 0 COMMENT '点赞数',
    `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_comment_id` (`parent_comment_id`),
    CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '评论表';

-- ============================================================
--  帖子点赞表
-- ============================================================
CREATE TABLE IF NOT EXISTS `post_like`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL COMMENT '点赞用户ID',
    `post_id`    BIGINT   NOT NULL COMMENT '被点赞帖子ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    -- 复合唯一键防止重复点赞
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`),
    CONSTRAINT `fk_post_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '帖子点赞表';

-- ============================================================
--  评论点赞表
-- ============================================================
CREATE TABLE IF NOT EXISTS `comment_like`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL COMMENT '点赞用户ID',
    `comment_id` BIGINT   NOT NULL COMMENT '被点赞评论ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
    KEY `idx_comment_id` (`comment_id`),
    CONSTRAINT `fk_comment_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '评论点赞表';

-- ============================================================
--  举报表
-- ============================================================
CREATE TABLE IF NOT EXISTS `report`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    `reporter_id`   BIGINT       NOT NULL COMMENT '举报者ID',
    `target_type`   VARCHAR(20)  NOT NULL COMMENT '目标类型：POST / COMMENT',
    `target_id`     BIGINT       NOT NULL COMMENT '目标ID',
    `reason`        VARCHAR(500) NOT NULL COMMENT '举报原因',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/RESOLVED/DISMISSED',
    `handler_id`    BIGINT                DEFAULT NULL COMMENT '处理者ID',
    `handle_remark` VARCHAR(500)          DEFAULT NULL COMMENT '处理备注',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    `handled_at`    DATETIME              DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '举报表';

-- ============================================================
-- No preset account. Register and promote your local test account following README.
