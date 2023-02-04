package com.example.studypartner.domain.dto;

import com.example.studypartner.common.PageRequest;
import com.example.studypartner.domain.vo.UserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 返回包装类
 *
 * @author wuxie
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamDTO extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 创始人用户信息
     */
    UserVO userVO;
}

