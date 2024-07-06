package com.example.studypartner.controller;

import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.UserLocation;
import com.example.studypartner.domain.request.UserLocationRequest;
import com.example.studypartner.domain.vo.UserLocationVO;
import com.example.studypartner.service.UserLocationService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author wuxie
 * @date 2024/1/14 14:20
 * @description 该文件的描述 todo
 */

@RequestMapping("/location")
@RestController
public class UserLocationController {

	@Resource
	private UserLocationService service;

	@Resource
	private UserService userService;

	@PostMapping("/load")
	public void loadData() {
		service.loadData();
	}

	//写一个获得坐标接口
	@GetMapping("/get")
	public CommonResult<UserLocationVO> getLocation(HttpServletRequest request) {
		return null;
	}



	@PostMapping("/save")
	public CommonResult<Boolean> saveLocation(@RequestBody UserLocationRequest userLocationRequest) {
		if (userLocationRequest == null) {
			return ResultUtils.failed(ErrorCode.NULL_ERROR, "数据为空");
		}
		UserLocation userLocation = UserLocation.fromUserLocationRequest(userLocationRequest);
		boolean save = service.save(userLocation);
		return ResultUtils.success(save);
	}
}
