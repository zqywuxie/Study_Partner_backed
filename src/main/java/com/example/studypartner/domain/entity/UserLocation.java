package com.example.studypartner.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.studypartner.domain.request.UserLocationRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户位置表
 * @TableName user_location
 */
@TableName(value ="user_location")
@Data
public class UserLocation implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 是否删除
     */
    private Integer deleted;

    /**
     * 国家
     */
    private String nation;

    /**
     * 最后登录时间
     */
    private Date updateTime;

    /**
     * 省份
     */
    private String provice;

    /**
     * 城市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * ipv6
     */
    private String ip;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


	public static UserLocation fromUserLocationRequest(UserLocationRequest request) {
		UserLocation userLocation = new UserLocation();
		userLocation.setIp(request.getIp());

		if (request.getLocation() != null) {
			userLocation.setLatitude(request.getLocation().getLat());
			userLocation.setLongitude(request.getLocation().getLng());
		}

		if (request.getAd_info() != null) {
			userLocation.setNation(request.getAd_info().getNation());
			userLocation.setProvice(request.getAd_info().getProvince());
			userLocation.setCity(request.getAd_info().getCity());
			userLocation.setDistrict(request.getAd_info().getDistrict());
		}

		return userLocation;
	}
}
