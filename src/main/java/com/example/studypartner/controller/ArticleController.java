package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.domain.Article;
import com.example.studypartner.service.ArticleService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wuxie
 * 文章数据接口
 */

@Api(value = "/article", tags = {"文章数据接口"})
@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    ArticleService articleService;

    /**
     *  文章数据获取
     * @param userAccount
     * @return
     */
        @GetMapping("/search")
    public CommonResult<List<Article>> article(String userAccount)
    {
        QueryWrapper<Article> articleQueryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(userAccount)) {
            articleQueryWrapper.like("userAccount", userAccount);
        }
        List<Article> list = articleService.list(articleQueryWrapper);
        return ResultUtils.success(list);
    }
    // @PostMapping("/insert")
    // public CommonResult<Boolean> insert()
}
