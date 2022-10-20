package com.example.usercenterback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenterback.domain.Article;
import com.example.usercenterback.mapper.ArticleMapper;
import com.example.usercenterback.service.ArticleService;
import org.springframework.stereotype.Service;

/**
* @author 思无邪
* @description 针对表【article(用户表)】的数据库操作Service实现
* @createDate 2022-10-16 22:36:22
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article  >
    implements ArticleService {

}




