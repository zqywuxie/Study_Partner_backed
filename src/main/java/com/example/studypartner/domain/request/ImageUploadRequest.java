package com.example.studypartner.domain.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author wuxie
 * @date 2023/11/22 15:48
 * @description 该文件的描述 todo
 */

@Data
@ApiModel(value = "图片上传请求")
public class ImageUploadRequest {

	@ApiModelProperty("图片")
	MultipartFile file;
	@ApiModelProperty("账号")
	String userAccount;

	String type;
}
