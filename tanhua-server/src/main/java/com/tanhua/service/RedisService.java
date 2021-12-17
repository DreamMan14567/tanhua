package com.tanhua.service;

import com.tanhua.domain.mongo.UserLike;

import java.util.List;

public interface RedisService {
    /**
     * 点赞 状态为 1
     *
     * @param likeUserId
     * @param targetId
     */
    void saveLike2Redis(String likeUserId, String targetId);

    /**
     * 取消点赞 状态为0
     *
     * @param likeUserId
     * @param targetId
     */
    void unlikeFromRedis(String likeUserId, String targetId);

    /**
     * 从redis中删除一条点赞数据
     *
     * @param likeUserId
     * @param targetId
     */
    void deleteLikeFromRedis(String likeUserId, String targetId);

    /**
     * redis中点赞记录+1
     *
     * @param likeUserId
     * @param targetId
     */
    void incrementLikeCount(String likeUserId, String targetId);

    /**
     * redis中点赞记录-1
     *
     * @param likeUserId
     * @param targetId
     */
    void decrementLikeCount(String likeUserId, String targetId);

    /**
     * 获取redis中存储的点赞记录
     *
     * @return
     */
    List<UserLike> getLikeDataFromRedis();

    /**
     * 获取redis中的点赞数
     *
     * @return
     */
    List<Long> getLikeCountFromRedis();

}
