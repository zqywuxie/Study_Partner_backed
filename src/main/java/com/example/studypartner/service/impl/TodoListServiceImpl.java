package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.domain.entity.TodoList;
import com.example.studypartner.mapper.TodoListMapper;
import com.example.studypartner.service.TodoListService;
import org.springframework.stereotype.Service;

/**
* @author wuxie
* @description 针对表【todo_list(待办事项表)】的数据库操作Service实现
* @createDate 2024-01-14 10:56:26
*/
@Service
public class TodoListServiceImpl extends ServiceImpl<TodoListMapper, TodoList>
    implements TodoListService {

}




