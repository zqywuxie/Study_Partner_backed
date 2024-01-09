package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.common.PageRequest;
import com.example.studypartner.domain.dto.BlogDTO;
import com.example.studypartner.domain.entity.Blog;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.BlogAddRequest;
import com.example.studypartner.domain.request.BlogUpdateRequest;
import com.example.studypartner.domain.vo.BlogVO;

/**
 * @author wuxie
 * @description 针对表【blog】的数据库操作Service
 * @createDate 2023-11-20 10:02:23
 */
public interface BlogService extends IService<Blog> {

	Long addBlog(BlogAddRequest blogAddRequest, User loginUser);

	Page<BlogVO> listMyBlogs(long currentPage, Long id);

	void likeBlog(long blogId, Long userId);

	Page<BlogVO> pageBlog(BlogDTO blogDTO, Long id);

	BlogVO getBlogById(long blogId, Long userId);

	BlogVO getBlogById(long blogId);

	void deleteBlog(Long blogId, Long userId, boolean isAdmin);

	void updateBlog(BlogUpdateRequest blogUpdateRequest, Long userId, boolean isAdmin);
}
