package com.example.studypartner.controller;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.service.OssService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.concurrent.TimeUnit;

import static com.example.studypartner.constant.RedisConstants.AVATAR_UPDATE_KEY;
import static com.example.studypartner.constant.RedisConstants.AVATAR_UPDATE_TTL;

@RestController
@RequestMapping("/fileOss")
public class OssController {

	@Resource
	private OssService ossService;

	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private UserService userService;

	String UPDATE_AVATAR = "update_avatar";


	//上传头像
	// todo 博客也要上传图片 设置一个类型属性？
	@ApiOperation(value = "文件上传")
	@PostMapping("/upload")
	public CommonResult<String> uploadOssFile(@RequestParam MultipartFile file, @RequestParam(required = false) String useraccount, @RequestParam(required = false) String type, HttpServletRequest httpServletRequest) {
		//获取上传的文件
		if (file.isEmpty()) {
			return null;
		}

		// todo 对用户修改头像的次数限制(oss 存储资源)
//		String key = AVATAR_UPDATE_KEY + useraccount;
//		String url = (String) redisTemplate.opsForValue().get(key);
//		if (url != null) {
//			return ResultUtils.failed(ErrorCode.REPEAT_ERROR, "今天已经改过了，明天再来吧");
//		}
		//返回上传到oss的路径
		String url = ossService.uploadFileAvatar(file);
//		redisTemplate.opsForValue().set(key, url, AVATAR_UPDATE_TTL, TimeUnit.HOURS);
		if (UPDATE_AVATAR.equals(type)) {
			Long count = userService.lambdaQuery().eq(User::getUseraccount, useraccount).count();
			if (count != 0) {
				LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
				userLambdaUpdateWrapper.eq(User::getUseraccount, useraccount).set(User::getAvatarUrl, url);
				boolean update = userService.update(userLambdaUpdateWrapper);
				return ResultUtils.success(update ? "更新头像成功" : "更新头像失败");
			}
		}

		//返回r对象
		return ResultUtils.success(url);
	}
}
