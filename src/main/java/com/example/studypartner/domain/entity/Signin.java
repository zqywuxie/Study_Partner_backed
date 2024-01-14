package com.example.studypartner.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName signin
 */
@TableName(value ="signin")
@Data
public class Signin implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 签到的年
     */
    private Object year;

    /**
     * 签到的月
     */
    private Integer month;

    /**
     * 签到的日期
     */
    private Date date;

    /**
     * 是否补签
     */
    private Integer isBackup;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}