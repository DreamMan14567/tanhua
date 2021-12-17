package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */
public interface UserInfoApi {
    /**
     * 新用户---1填写资料
     *
     * @param userInfo 用户数据信息
     * @return
     */
    void fillUserInformation(UserInfo userInfo);


    /**
     * 对头像进行选取并更新
     *
     * @param userInfo
     */
    void updateAvatar(UserInfo userInfo);

    /**
     * 根据ID查找用户信息
     *
     * @param loginUserId
     * @return
     */
    UserInfo findUserInfoById(Long loginUserId);

    /**
     * 用户资料 - 保存
     *
     * @param userInfo
     */
    void update(UserInfo userInfo);

    /**
     * 批量查询用户
     *
     * @param blackListIds
     * @return
     */
    List<UserInfo> findUSerByBatchId(List<Long> blackListIds);

    /**
     * 分页查询用户详情信息
     *
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPage(Long page, Long pagesize);
}
