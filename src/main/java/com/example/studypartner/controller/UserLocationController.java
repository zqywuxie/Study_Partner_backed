package com.example.studypartner.controller;

import com.example.studypartner.service.UserLocationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

	@PostMapping("/load")
	public void loadData() {
		service.loadData();
	}
}
