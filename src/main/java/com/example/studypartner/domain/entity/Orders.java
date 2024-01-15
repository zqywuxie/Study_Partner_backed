package com.example.studypartner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单表
 * @TableName orders
 */
@TableName(value ="orders")
@Data
public class Orders implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 商品id
     */
    private Long goodsId;

    /**
     * 订单价格
     */
    private BigDecimal price;

    /**
     * 订单创建时间
     */
    private Date createTime;

    /**
     * 逻辑删除
     */
	@ApiModelProperty("是否删除 设置逻辑删除")
	@TableLogic(value = "0", delval = "1")
	@TableField("deleted")
    private Integer deleted;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 状态 0-正常 1-支付成功 2-取消 3-异常
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}