package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.domain.Article;
import com.example.studypartner.mapper.ArticleMapper;
import com.example.studypartner.service.ArticleService;
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




