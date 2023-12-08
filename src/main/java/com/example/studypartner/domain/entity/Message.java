package com.example.studypartner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author wuxie
 * @TableName message
 */
@TableName(value ="message")
@Data
public class Message implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息类型-1 点赞
     */
    private Integer type;

    /**
     * 消息发送的用户id
     */
    private Long fromId;

    /**
     * 消息接收的用户id
     */
    private Long toId;

    /**
     * 消息内容
     */
    private String data;

    /**
     * 已读-0 未读 ,1 已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
	@ApiModelProperty("是否删除 设置逻辑删除")
	@TableLogic(value = "0", delval = "1")
	@TableField("deleted")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}