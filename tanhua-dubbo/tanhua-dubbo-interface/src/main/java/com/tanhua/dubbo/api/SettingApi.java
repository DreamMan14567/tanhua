package com.tanhua.dubbo.api;

import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.vo.PageResult;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
public interface SettingApi {
    /**
     * 查询用户陌生人问题
     *
     * @param userId
     * @return
     */
    Settings queryUserById(Long userId);

    /**
     * 添加通知
     *
     * @param settings
     */
    void insertSetting(Settings settings);

    /**
     * 更新通知
     *
     * @param settings
     */
    void updateSetting(Settings settings);

    /**
     * 分页查询黑名单
     *
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findBlackListPage(Long UserId, Long page, Long pagesize);

    /**
     * 移除黑名单
     *
     * @param blackList
     */
    void deleteBlacker(BlackList blackList);
}
