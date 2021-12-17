package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
public interface IMApi {
    /**
     * 分页获取公告列表
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Announcement> queryAnnouncement(Long page, Long pagesize);

    void addFriend(Long loginUserId, Long userId);
}
