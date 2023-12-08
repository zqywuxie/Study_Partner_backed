package com.example.studypartner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天记录表
 * @TableName chat
 */
@TableName(value ="chat")
@Data
// todo
// 1.两个人的消息分成两份，可以方便进行清空聊天记录
// 2.分成两份就导致资源浪费了
// 解决？
public class Chat implements Serializable {
    /**
     * 聊天记录id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送消息id
     */
    private Long fromId;

    /**
     * 接收消息id
     */
    private Long toId;

    /**
     * 
     */
    private String content;

    /**
     * 聊天类型 1-私聊 2-群聊
     */
    private Integer chatType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 群聊id
     */
    private Long teamId;

    /**
     * 
     */
	@ApiModelProperty("是否删除 设置逻辑删除")
	@TableLogic(value = "0", delval = "1")
	@TableField("deleted")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}