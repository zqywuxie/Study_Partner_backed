package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.dto.UserDTO;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.RegisterRequest;
import com.example.studypartner.domain.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 思无邪
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2022-10-10 16:54:41
 */
public interface UserService extends IService<User> {

	/**
	 * 注册
	 *
	 * @param request
	 * @return
	 */
	String register(RegisterRequest request);

	/**
	 * 登录
	 *
	 * @param useraccount
	 * @param password
	 * @param request
	 * @return
	 */
	User login(String useraccount, String password, HttpServletRequest request);

	/**
	 * 根据邮箱登录
	 *
	 * @param email
	 * @param request
	 * @return
	 */
	User loginByEmail(String email, HttpServletRequest request);


	/**
	 * 用户信息脱敏
	 *
	 * @param user
	 * @return
	 */
	User cleanUser(User user);


	/**
	 * 根据标签名进行查找用户
	 *
	 * @param tagNameList
	 * @return
	 */
	List<User> searchUserByTags(List<String> tagNameList);

	UserVO searchUserById(Long id, User loginUser);

	List<User> memorySearch(List<String> tagNameList);

	/**
	 * 更新数据
	 *
	 * @param user
	 * @param request
	 * @return
	 */
	Integer updateUser(User user, HttpServletRequest request);

	/**
	 * 获得当前用户信息
	 *
	 * @return
	 */
	User getLoginUser(HttpServletRequest request);

	/**
	 * 鉴权
	 *
	 * @param request
	 * @return
	 */
	boolean isAdmin(HttpServletRequest request);

	boolean isAdmin(User loginUser);

	List<User> matchUsers(long num, User loginUser);

	void updatePassword(String phone, String password);


	Page<User> searchByText(UserDTO userDTO);

	Page<User> recommend(Long pageSize, Long currentPage, Long userId);
}
