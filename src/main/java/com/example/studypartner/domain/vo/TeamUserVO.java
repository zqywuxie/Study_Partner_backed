package com.example.studypartner.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author niumazlb
 * @create 2022-08-29 20:23
 */
@Data
public class TeamUserVO implements Serializable {


    private static final long serialVersionUID = 7389362995098717607L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

//    /**
//     * 公告
//     */
//    private String announce;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date expireTime;

    /**
     * 创建人
     */
    private List<UserVO> manageUserList;
    /**
     * 创建人
     */
    private long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 已加入的用户数
     */
    private Integer hasJoinNum = 0;

    /**
     * 已加入用户列表
     */
    private List<UserVO> joinUserList;

    private boolean hasJoin;
}
