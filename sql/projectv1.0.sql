create table if not exists blog
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    userId      bigint unsigned                          not null comment '用户id',
    title       varchar(255) collate utf8mb4_unicode_ci  not null comment '标题',
    images      varchar(2048)                            null comment '图片，最多9张，多张以","隔开',
    content     varchar(2048) collate utf8mb4_unicode_ci not null comment '文章',
    likedNum    int unsigned default '0'                 null comment '点赞数量',
    commentsNum int unsigned default '0'                 null comment '评论数量',
    createTime  timestamp    default CURRENT_TIMESTAMP   not null comment '创建时间',
    updateTime  timestamp    default CURRENT_TIMESTAMP   not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                   not null
)
    row_format = COMPACT;

create table if not exists blog_like
(
    id         bigint auto_increment comment '主键'
        primary key,
    blogId     bigint                             not null comment '博文id',
    userId     bigint                             not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime                           null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除'
)
    charset = utf8
    row_format = COMPACT;

create table if not exists chat
(
    id         bigint auto_increment comment '聊天记录id'
        primary key,
    fromId     bigint                                  not null comment '发送消息id',
    toId       bigint                                  null comment '接收消息id',
    content    varchar(512) collate utf8mb4_unicode_ci null,
    chatType   tinyint                                 not null comment '聊天类型 1-私聊 2-群聊',
    createTime datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP      null,
    teamId     bigint                                  null comment '群聊id',
    isDelete   tinyint  default 0                      null
)
    comment '聊天记录表' row_format = COMPACT;

create table if not exists comment_like
(
    id         bigint auto_increment comment '主键'
        primary key,
    commentId  bigint                             not null comment '评论id',
    userId     bigint                             not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除'
)
    row_format = COMPACT;

create table if not exists comments
(
    id              int auto_increment
        primary key,
    blogId          bigint unsigned                     not null,
    userId          bigint                              not null,
    content         text                                not null,
    likedNum        int       default 0                 null,
    createTime      timestamp default CURRENT_TIMESTAMP null,
    updateTime      timestamp default CURRENT_TIMESTAMP null,
    status          tinyint unsigned                    null comment '状态，0：正常，1：被举报，2：禁止查看',
    parentCommentId int                                 null,
    childCommentId  int                                 null
)
    row_format = COMPACT;

create table if not exists follow
(
    id           bigint auto_increment comment '主键'
        primary key,
    userId       bigint unsigned                     not null comment '用户id',
    followUserId bigint unsigned                     not null comment '关注的用户id',
    createTime   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint   default 0                 null comment '逻辑删除'
)
    charset = utf8
    row_format = COMPACT;

create table if not exists friend_application
(
    id         bigint auto_increment comment '好友申请id'
        primary key,
    fromId     bigint                             not null comment '发送申请的用户id',
    receiveId  bigint                             null comment '接收申请的用户id ',
    isRead     tinyint  default 0                 not null comment '是否已读(0-未读 1-已读)',
    status     tinyint  default 0                 not null comment '申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-不同意）',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null,
    isDelete   tinyint  default 0                 not null comment '是否删除',
    remark     varchar(214)                       null comment '好友申请备注信息'
)
    comment '好友申请管理表' row_format = COMPACT;

create table if not exists message
(
    id         bigint auto_increment comment '主键'
        primary key,
    type       tinyint                            null comment '类型 点赞1/评论2/关注3/系统信息4',
    fromId     bigint                             null comment '消息发送的用户id',
    toId       bigint                             null comment '消息接收的用户id',
    data       varchar(255)                       null comment '消息内容',
    isRead     tinyint  default 0                 null comment '已读-0 未读 ,1 已读',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除'
)
    row_format = COMPACT;

create table if not exists team
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
    isDelete    tinyint  default 0                 not null comment '是否删除',
    avatarUrl   varchar(512)                       null comment '队伍头像'
)
    comment '队伍';

create table if not exists user
(
    id           bigint auto_increment
        primary key,
    username     varchar(512) charset utf8          null,
    userAccount  varchar(512) charset utf8          null,
    avatarUrl    varchar(512) charset utf8          null,
    gender       varchar(512) charset utf8          null,
    userPassword varchar(512)                       not null comment '密码',
    email        varchar(512) charset utf8          null,
    userStatus   int      default 0                 null comment '状态 0-正常',
    phone        varchar(128)                       null comment '电话',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 not null comment '用户权限 0普通用户 1管理员
',
    city         varchar(512) charset utf8          null comment '所在城市',
    profile      varchar(512) charset utf8          null comment '市',
    province     varchar(512) charset utf8          null comment '省',
    tags         varchar(1024)                      null comment '标签 json 列表',
    friendsIds   varchar(512)                       null comment '好友id'
)
    comment '用户表';

create table if not exists user_team
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

