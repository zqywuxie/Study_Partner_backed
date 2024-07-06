package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.FriendApplication;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.FriendAddRequest;
import com.example.studypartner.domain.vo.FriendsRecordVO;


import java.util.List;
import java.util.Set;

/**
* @author wuxie
* @description 针对表【friends(好友申请管理表)】的数据库操作Service
* @createDate 2023-11-18 14:10:45
*/
public interface FriendApplicationService extends IService<FriendApplication> {

    /**
     * 好友申请
     *
     * @param loginUser
     * @param friendAddRequest
     * @return
     */
    boolean addFriendRecords(User loginUser, FriendAddRequest friendAddRequest);

	/**
	 * 好友申请
	 *
	 * @param loginUser
	 * @param friendId
	 * @return
	 */
	boolean deleteFriendRecords(User loginUser, Long friendId);

    /**
     * 查询出所有申请、同意记录
     *
     * @param loginUser
     * @return
     */
    List<FriendsRecordVO> obtainFriendApplicationRecords(User loginUser);

    /**
     * 同意好友申请
     *
     * @param loginUser
     * @param fromId
     * @return
     */
    boolean agreeToApply(User loginUser, Long fromId);

    /**
     * 不同意好友申请
     *
     * @param id        申请用户id
     * @param loginUser 登录用户
     * @return
     */
    boolean canceledApply(Long id, User loginUser);

    /**
     * 获取我申请的记录
	 * obtainTheRecordOfMyApplication
     *
     * @param loginUser
     * @return
     */
    List<FriendsRecordVO> obtainTheRecordOfMyApplication(User loginUser);

    /**
     * 获取未读记录条数
	 * obtainTheNumberOfUnreadRecords
     *
     * @param loginUser
     * @return
     */
    int obtainTheNumberOfUnreadRecords(User loginUser);

    /**
     * 读取纪录
     *
     * @param loginUser
     * @param ids
     * @return
     */
    boolean toRead(User loginUser, Set<Long> ids);
}
