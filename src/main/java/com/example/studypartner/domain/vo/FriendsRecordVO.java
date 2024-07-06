package com.example.studypartner.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友vo
 *
 * @author wuxie
 * @date 2023/06/22
 */
@Data
public class FriendsRecordVO implements Serializable {
	private static final long serialVersionUID = 1928465648232335L;


	/**
	 * 申请状态 默认0 （0-未通过 1-已同意 2-已过期）
	 */
	@ApiModelProperty(value = "申请状态")
	private Integer status;

	/**
	 * 好友申请备注信息
	 */
	@ApiModelProperty(value = "好友申请备注信息")
	private String remark;

	/**
	 * 申请用户
	 */
	@ApiModelProperty(value = "申请用户")
	private UserVO applyUser;

	@ApiModelProperty(value = "申请时间")
	private Date createTime;
}
