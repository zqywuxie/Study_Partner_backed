create database usercenter;
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户 id',
    parentId   bigint                             null comment '父标签 id',
    isParent   tinyint                            null comment '0 - 不是, 1 - 父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除',
    constraint uniIdx_tagName
        unique (tagName)
)
    comment '标签';

create index idx_userId
    on tag (userId);

create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    expireTime  datetime                           null comment '过期时间',
    userId      bigint                             null comment '用户id',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍';

create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(512) charset utf8mb3       null,
    userAccount  varchar(512) charset utf8mb3       null,
    avatarUrl    varchar(512) charset utf8mb3       null,
    gender       varchar(512) charset utf8mb3       null,
    userPassword varchar(512)                       not null comment '密码',
    email        varchar(512) charset utf8mb3       null,
    userStatus   int      default 0                 null comment '状态 0-正常',
    phone        varchar(128)                       null comment '电话',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 not null comment '用户权限 0普通用户 1管理员
',
    city         varchar(512) charset utf8mb3       null comment '所在城市',
    profile      varchar(512) charset utf8mb3       null comment '市',
    province     varchar(512) charset utf8mb3       null comment '省',
    tags         varchar(1024)                      null comment '标签 json 列表'
)
    comment '用户表';

create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                             null comment '用户id',
    teamId     bigint                             null comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系';


-- ----------------------------
-- 博客表
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog`
(
    `id`           bigint(20) UNSIGNED                                            NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      bigint(20) UNSIGNED                                            NOT NULL COMMENT '用户id',
    `title`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '标题',
    `images`       varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '图片，最多9张，多张以\",\"隔开',
    `content`      varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章',
    `liked_num`    int(8) UNSIGNED                                                NULL     DEFAULT 0 COMMENT '点赞数量',
    `comments_num` int(8) UNSIGNED                                                NULL     DEFAULT 0 COMMENT '评论数量',
    `create_time`  timestamp                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 19
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = COMPACT;


-- ----------------------------
-- 用户关注表
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`
(
    `id`             bigint(20)          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
    `follow_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '关注的用户id',
    `create_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`      tinyint(4)          NULL     DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 44
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = COMPACT;


DROP TABLE IF EXISTS `blogLike`;
CREATE TABLE `blogLike`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `blog_id`     bigint(20) NOT NULL COMMENT '博文id',
    `user_id`     bigint(20) NOT NULL COMMENT '用户id',
    `create_time` datetime   NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime   NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = COMPACT;

