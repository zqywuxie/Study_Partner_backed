create table if not exists blog
(
    id           bigint unsigned auto_increment comment '主键'
        primary key,
    user_id      bigint unsigned                          not null comment '用户id',
    title        varchar(255) collate utf8mb4_unicode_ci  not null comment '标题',
    images       varchar(2048)                            null comment '图片，最多9张，多张以","隔开',
    content      varchar(2048) collate utf8mb4_unicode_ci not null comment '文章',
    liked_num    int unsigned default '0'                 null comment '点赞数量',
    comments_num int unsigned default '0'                 null comment '评论数量',
    create_time  timestamp    default CURRENT_TIMESTAMP   not null comment '创建时间',
    update_time  timestamp    default CURRENT_TIMESTAMP   not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      tinyint      default 0                   not null
)
    collate = utf8mb4_general_ci
    row_format = COMPACT;

create table if not exists blog_like
(
    id          bigint auto_increment comment '主键'
        primary key,
    blog_id     bigint                             not null comment '博文id',
    user_id     bigint                             not null comment '用户id',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime                           null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 null comment '逻辑删除'
)
    charset = utf8mb3
    row_format = COMPACT;

create table if not exists chat
(
    id          bigint auto_increment comment '聊天记录id'
        primary key,
    from_id     bigint                                  not null comment '发送消息id',
    to_id       bigint                                  null comment '接收消息id',
    content     varchar(512) collate utf8mb4_unicode_ci null,
    chat_type   tinyint                                 not null comment '聊天类型 1-私聊 2-群聊',
    create_time datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP      null,
    team_id     bigint                                  null comment '群聊id',
    deleted     tinyint  default 0                      null
)
    comment '聊天记录表' collate = utf8mb4_general_ci
                         row_format = COMPACT;

create table if not exists comment_like
(
    id          bigint auto_increment comment '主键'
        primary key,
    comment_id  bigint                             not null comment '评论id',
    user_id     bigint                             not null comment '用户id',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 null comment '逻辑删除'
)
    collate = utf8mb4_general_ci
    row_format = COMPACT;

create table if not exists follow
(
    id             bigint auto_increment comment '主键'
        primary key,
    user_id        bigint unsigned                     not null comment '用户id',
    follow_user_id bigint unsigned                     not null comment '关注的用户id',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint   default 0                 null comment '逻辑删除'
)
    charset = utf8mb3
    row_format = COMPACT;

create table if not exists friend_application
(
    id          bigint auto_increment comment '好友申请id'
        primary key,
    from_id     bigint                             not null comment '发送申请的用户id',
    receive_id  bigint                             null comment '接收申请的用户id ',
    is_read     tinyint  default 0                 not null comment '是否已读(0-未读 1-已读)',
    status      tinyint  default 0                 not null comment '申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-不同意）',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null,
    deleted     tinyint  default 0                 not null comment '是否删除',
    remark      varchar(214)                       null comment '好友申请备注信息'
)
    comment '好友申请管理表 0未处理 1同意 2拒绝 3过期' collate = utf8mb4_general_ci
                                                       row_format = COMPACT;

create table if not exists message
(
    id          bigint auto_increment comment '主键'
        primary key,
    type        tinyint                            null comment '类型 点赞1/评论2/关注3/系统信息4',
    from_id     bigint                             null comment '消息发送的用户id',
    to_id       bigint                             null comment '消息接收的用户id',
    data        varchar(255)                       null comment '消息内容',
    is_read     tinyint  default 0                 null comment '已读-0 未读 ,1 已读',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 null comment '逻辑删除'
)
    collate = utf8mb4_general_ci
    row_format = COMPACT;

create table if not exists signin
(
    id        bigint unsigned auto_increment comment '主键'
        primary key,
    user_id   bigint unsigned  not null comment '用户id',
    year      year             not null comment '签到的年',
    month     tinyint          not null comment '签到的月',
    date      date             not null comment '签到的日期',
    is_backup tinyint unsigned null comment '是否补签'
);

create table if not exists sys_dept
(
    id          bigint auto_increment comment '主键'
        primary key,
    name        varchar(64)  default '' not null comment '部门名称',
    parent_id   bigint       default 0  not null comment '父节点id',
    tree_path   varchar(255) default '' null comment '父节点id路径',
    sort        int          default 0  null comment '显示顺序',
    status      tinyint      default 1  not null comment '状态(1:正常;0:禁用)',
    deleted     tinyint      default 0  null comment '逻辑删除标识(1:已删除;0:未删除)',
    create_time datetime                null comment '创建时间',
    update_time datetime                null comment '更新时间',
    create_by   bigint                  null comment '创建人ID',
    update_by   bigint                  null comment '修改人ID'
)
    comment '部门表' collate = utf8mb4_general_ci
                     row_format = DYNAMIC;

create table if not exists sys_dict
(
    id          bigint auto_increment comment '主键'
        primary key,
    type_code   varchar(64)             null comment '字典类型编码',
    name        varchar(50)  default '' null comment '字典项名称',
    value       varchar(50)  default '' null comment '字典项值',
    sort        int          default 0  null comment '排序',
    status      tinyint      default 0  null comment '状态(1:正常;0:禁用)',
    defaulted   tinyint      default 0  null comment '是否默认(1:是;0:否)',
    remark      varchar(255) default '' null comment '备注',
    create_time datetime                null comment '创建时间',
    update_time datetime                null comment '更新时间'
)
    comment '字典数据表' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

create table if not exists sys_dict_type
(
    id          bigint auto_increment comment '主键 '
        primary key,
    name        varchar(50) default '' null comment '类型名称',
    code        varchar(50) default '' null comment '类型编码',
    status      tinyint(1)  default 0  null comment '状态(0:正常;1:禁用)',
    remark      varchar(255)           null comment '备注',
    create_time datetime               null comment '创建时间',
    update_time datetime               null comment '更新时间',
    constraint type_code
        unique (code)
)
    comment '字典类型表' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

create table if not exists sys_menu
(
    id          bigint auto_increment
        primary key,
    parent_id   bigint                  not null comment '父菜单ID',
    tree_path   varchar(255)            null comment '父节点ID路径',
    name        varchar(64)  default '' not null comment '菜单名称',
    type        tinyint                 not null comment '菜单类型(1:菜单 2:目录 3:外链 4:按钮)',
    path        varchar(128) default '' null comment '路由路径(浏览器地址栏路径)',
    component   varchar(128)            null comment '组件路径(vue页面完整路径，省略.vue后缀)',
    perm        varchar(128)            null comment '权限标识',
    visible     tinyint(1)   default 1  not null comment '显示状态(1-显示;0-隐藏)',
    sort        int          default 0  null comment '排序',
    icon        varchar(64)  default '' null comment '菜单图标',
    redirect    varchar(128)            null comment '跳转路径',
    create_time datetime                null comment '创建时间',
    update_time datetime                null comment '更新时间',
    always_show tinyint                 null comment '【目录】只有一个子路由是否始终显示(1:是 0:否)',
    keep_alive  tinyint                 null comment '【菜单】是否开启页面缓存(1:是 0:否)'
)
    comment '菜单管理' collate = utf8mb4_general_ci
                       row_format = DYNAMIC;

create table if not exists sys_role
(
    id          bigint auto_increment
        primary key,
    name        varchar(64) default '' not null comment '角色名称',
    code        varchar(32)            null comment '角色编码',
    sort        int                    null comment '显示顺序',
    status      tinyint(1)  default 1  null comment '角色状态(1-正常；0-停用)',
    data_scope  tinyint                null comment '数据权限(0-所有数据；1-部门及子部门数据；2-本部门数据；3-本人数据)',
    deleted     tinyint(1)  default 0  not null comment '逻辑删除标识(0-未删除；1-已删除)',
    create_time datetime               null comment '更新时间',
    update_time datetime               null comment '创建时间',
    constraint name
        unique (name)
)
    comment '角色表' collate = utf8mb4_general_ci
                     row_format = DYNAMIC;

create table if not exists sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID'
)
    comment '角色和菜单关联表' collate = utf8mb4_general_ci
                               row_format = DYNAMIC;

create table if not exists sys_user
(
    id          int auto_increment
        primary key,
    profile     varchar(1024)                          null comment '个人简历',
    username    varchar(64)                            null comment '用户名',
    useraccount varchar(512)                           null comment '用户账号',
    password    varchar(100)                           null comment '密码',
    tags        varchar(1024)                          null comment '标签',
    dept_id     int                                    null comment '部门ID',
    avatar_url  varchar(255) default ''                null comment '用户头像',
    gender      tinyint(1)   default 1                 null comment '性别((1:男;2:女))',
    mobile      varchar(20)                            null comment '联系方式',
    status      tinyint(1)   default 1                 null comment '用户状态((1:正常;0:禁用))',
    email       varchar(128)                           null comment '用户邮箱',
    deleted     tinyint(1)   default 0                 null comment '逻辑删除标识(0:未删除;1:已删除)',
    create_time datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime     default CURRENT_TIMESTAMP null comment '更新时间',
    friends_ids varchar(512)                           null comment '好友id',
    constraint login_name
        unique (username)
)
    comment '用户信息表' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

create table if not exists sys_user_role
(
    user_id bigint not null comment '用户ID',
    role_id bigint not null comment '角色ID',
    primary key (user_id, role_id)
)
    comment '用户和角色关联表' collate = utf8mb4_general_ci
                               row_format = DYNAMIC;

create table if not exists team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    max_num     int      default 1                 not null comment '最大人数',
    expire_time datetime                           null comment '过期时间',
    user_id     bigint                             null comment '用户id',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 封禁',
    password    varchar(512)                       null comment '密码',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 not null comment '是否删除',
    avatar_url  varchar(512)                       null comment '队伍头像'
)
    comment '队伍';

create table if not exists todo_list
(
    id         bigint auto_increment
        primary key,
    name       varchar(256)                       not null comment '待办事项内容',
    start_time datetime default CURRENT_TIMESTAMP not null comment '开始时间',
    end_time   datetime                           not null invisible comment '结束时间',
    flag       tinyint  default 0                 not null invisible comment '是否实现 0-代办 1-完成',
    deleted    tinyint  default 0                 not null comment '逻辑删除 0 1-删除'
)
    comment '待办事项表';

create table if not exists user
(
    id          bigint auto_increment
        primary key,
    username    varchar(512) charset utf8mb3                           null,
    useraccount varchar(512) charset utf8mb3                           null,
    avatar_url  varchar(512) charset utf8mb3                           null,
    gender      varchar(512) charset utf8mb3 default '2'               null comment '0-女，1-男，2-未知',
    password    varchar(512)                                           not null comment '密码',
    email       varchar(512) charset utf8mb3                           null,
    status      int                          default 1                 null comment '状态 1-正常 0 封禁',
    phone       varchar(128)                                           null comment '电话',
    create_time datetime                     default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime                     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint                      default 0                 not null comment '是否删除',
    profile     varchar(512) charset utf8mb3                           null comment '市',
    tags        varchar(1024)                                          null comment '标签 json 列表',
    friends_ids varchar(512)                                           null comment '好友id',
    dept_id     int                                                    null comment '部门id'
)
    comment '用户表';

create table if not exists comments
(
    id                int auto_increment
        primary key,
    blog_id           bigint unsigned                     not null,
    user_id           bigint                              not null,
    content           text                                not null,
    liked_num         int       default 0                 null,
    create_time       timestamp default CURRENT_TIMESTAMP null,
    update_time       timestamp default CURRENT_TIMESTAMP null,
    status            tinyint unsigned                    null comment '状态，0：正常，1：被举报，2：禁止查看',
    parent_comment_id int                                 null,
    child_comment_id  int                                 null,
    deleted           tinyint   default 0                 not null comment '逻辑删除',
    constraint comments_ibfk_1
        foreign key (blog_id) references blog (id),
    constraint comments_ibfk_2
        foreign key (user_id) references user (id),
    constraint comments_ibfk_3
        foreign key (parent_comment_id) references comments (id),
    constraint comments_ibfk_4
        foreign key (child_comment_id) references comments (id)
)
    collate = utf8mb4_general_ci
    row_format = COMPACT;

create index blogId
    on comments (blog_id);

create index childCommentId
    on comments (child_comment_id);

create index parentCommentId
    on comments (parent_comment_id);

create index userId
    on comments (user_id);

create table if not exists user_location
(
    id        bigint auto_increment comment 'id'
        primary key,
    user_id   bigint            null comment '用户id',
    latitude  double            null comment '纬度',
    longitude double            null comment '经度',
    deleted   tinyint default 0 not null comment '是否删除'
)
    comment '用户位置表';

create table if not exists user_team
(
    id          bigint auto_increment comment 'id'
        primary key,
    user_id     bigint                             null comment '用户id',
    team_id     bigint                             null comment '队伍id',
    join_time   datetime                           null comment '加入时间',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系';

