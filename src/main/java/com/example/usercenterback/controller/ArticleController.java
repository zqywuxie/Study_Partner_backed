package com.example.usercenterback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.usercenterback.common.CommonResult;
import com.example.usercenterback.domain.Article;
import com.example.usercenterback.service.ArticleService;
import com.example.usercenterback.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 2022/10/18
 *
 * @version 1.0
 * @Author:zqy
 */
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
