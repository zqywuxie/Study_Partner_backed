package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.Team;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.dto.TeamDTO;
import com.example.studypartner.domain.request.TeamJoinRequest;
import com.example.studypartner.domain.request.TeamQuitRequest;
import com.example.studypartner.domain.request.TeamUpdateRequest;
import com.example.studypartner.domain.vo.TeamUserVO;

import java.util.List;

/**
 * @author wuxie
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2023-02-03 11:45:44
 */
public interface TeamService extends IService<Team> {

	long addTeam(Team team, User loginUser);


	/**
	 * 搜素队伍方法
	 *
	 * @param teamDTO
	 * @param isAdmin
	 * @return
	 */
	List<TeamUserVO> listTeams(TeamDTO teamDTO, boolean isAdmin);

	boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);


	boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

	Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

	boolean deleteTeam(Long id, User loginUser);

	TeamUserVO getTeamById(long id, boolean isAdmin, User loginUser);

	List<TeamUserVO> myJoinTeams(TeamDTO teamDTO, Long loginUserId);

	List<TeamUserVO> myCreateTeams(TeamDTO teamDTO, Long loginUserId);

	Page<TeamUserVO> searchAllByPage(TeamDTO teamDTO, Long loginUserId);
}
