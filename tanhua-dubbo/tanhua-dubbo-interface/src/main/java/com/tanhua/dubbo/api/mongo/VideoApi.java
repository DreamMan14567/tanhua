package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

/**
 * @author user_Chubby
 * @date 2021/5/16
 * @Description
 */
public interface VideoApi {
    void save(Video video);

    PageResult findPage(Long page, Long pagesize);

    /**
     * 关注用户
     *
     * @param followUser
     */
    void follow(FollowUser followUser);

    /**
     * 取消关注用户
     *
     * @param followUser
     */
    void cancelFollow(FollowUser followUser);

    /**
     * 查找指定用户的视频列表
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    PageResult<Video> findUserVideo(Long page, Long pagesize, Long uid);
}
