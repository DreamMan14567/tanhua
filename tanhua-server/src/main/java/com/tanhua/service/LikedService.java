package com.tanhua.service;

import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

public interface LikedService {
    /**
     * 保存点赞记录
     *
     * @param userLike 点赞记录
     * @return
     */
    UserLike save(UserLike userLike);

    /**
     * 批量保存或者修改
     *
     * @param likeList 点赞列表
     * @return
     */
    List<UserLike> saveAll(List<UserLike> likeList);

    /**
     * 根据点赞用户ID获取点赞列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pagesize 页容量
     * @return
     */
    PageResult<UserLike> getLikedListByLikedUserId(String userId, Long page, Long pagesize);

    /**
     * 根据用户ID 和 目标ID 查询是否存在点赞记录
     *
     * @param likeUserId 点赞用户ID
     * @param targetId   点赞目标ID
     * @return
     */
    UserLike getByLikeUserIdAndMomentId(String likeUserId, String targetId);

    /**
     * 持久化点赞状态到数据库中
     */
    void transLikedFromRedis2DB();

    /**
     * 持久化点赞数量到数据库中
     */
    void transLikedCountFromRedis2DB();
}
