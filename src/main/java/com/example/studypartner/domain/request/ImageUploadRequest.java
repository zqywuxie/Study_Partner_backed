package com.example.studypartner.domain.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author wuxie
 * @date 2023/11/22 15:48
 * @description 该文件的描述 todo
 */

@Data
public class ImageUploadRequest {

	@ApiModelProperty("图片")
	MultipartFile file;
	@ApiModelProperty("账号")
	String useraccount;

	String type;
}
