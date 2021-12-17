package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */
public interface FriendApi {
    /**
     * 分页获取好友列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    PageResult findPage(Long page, Long pagesize, Long loginUserId);
}
