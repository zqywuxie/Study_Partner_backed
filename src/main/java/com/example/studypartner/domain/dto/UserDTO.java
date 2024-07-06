package com.example.studypartner.domain.dto;

import com.example.studypartner.common.PageRequest;
import lombok.Data;

/**
 * @author wuxie
 * @date 2024/1/7 15:50
 * @description 该文件的描述 todo
 */

@Data
public class UserDTO extends PageRequest {


	private String searchText;
}
