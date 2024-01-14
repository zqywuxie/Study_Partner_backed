package com.example.studypartner.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.domain.entity.SysUserRole;
import com.example.studypartner.mapper.SysUserRoleMapper;
import com.example.studypartner.service.SysUserRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

	/**
	 * 保存用户角色
	 *
	 * @param userId
	 * @param roleIds
	 * @return
	 */
	@Override
	public boolean saveUserRoles(Long userId, Long roleIds) {

		if (userId == null || roleIds == null) {
			return false;
		}
		this.save(new SysUserRole(userId, roleIds));
		return true;
	}
}
