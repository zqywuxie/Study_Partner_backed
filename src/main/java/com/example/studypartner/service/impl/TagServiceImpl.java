package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.domain.Tag;
import com.example.studypartner.mapper.TagMapper;
import com.example.studypartner.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author wuxie
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-01-26 10:53:24
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




