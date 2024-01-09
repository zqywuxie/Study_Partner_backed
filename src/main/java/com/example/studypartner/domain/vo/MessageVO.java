package com.example.studypartner.domain.vo;

import com.example.studypartner.domain.entity.Message;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 消息vo
 *
 * @author wuxie
 * @date 2023/06/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageVO extends Message {
	/**
	 * 串行版本uid
	 */
	private static final long serialVersionUID = 4353136955942044222L;


	// 评论 点赞 关注 好友申请

	//fromUser 评论内容 博文封面

	//fromUser 点赞内容（博文标题/评论）

	//fromUser

	//fromUser 申请内容

	/**
	 * 发送者的信息
	 */
	@ApiModelProperty(value = "发送者的信息")
	private UserVO fromUser;

	/**
	 * 博客
	 */
	@ApiModelProperty(value = "博客")
	private BlogVO blog;
	/**
	 * 评论
	 */
	@ApiModelProperty(value = "评论")
	private CommentsVO comment;


	@ApiModelProperty(value = "好友申请")
	private FriendsRecordVO friendsRecordVO;


}
