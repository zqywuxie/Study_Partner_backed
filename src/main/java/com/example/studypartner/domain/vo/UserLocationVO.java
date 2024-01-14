package com.example.studypartner.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.studypartner.domain.entity.User;
import lombok.Data;
import org.springframework.data.geo.Distance;

import java.io.Serializable;

/**
 * 用户位置表
 *
 * @author wuxie
 */
@Data
public class UserLocationVO implements Serializable {

	/**
	 * 用户
	 */
	private User user;

	/**
	 * 纬度
	 */
	private Double latitude;

	/**
	 * 经度
	 */
	private Double longitude;

	Double distance;

	private static final long serialVersionUID = 1L;
}