package com.example.studypartner.domain.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 博客添加请求
 *
 * @author wuxie
 * @date 2023/06/22
 */
@Data
public class BlogAddRequest implements Serializable {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = 8975136896057535409L;
    /**
     * 图片
     */
    @ApiModelProperty(value = "图片")
    private MultipartFile[] images;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;
    /**
     * 内容
     */
    @ApiModelProperty(value = "正文")
    private String content;
}
