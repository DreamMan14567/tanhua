package com.tanhua.service;

import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikedServiceImpl implements LikedService {
    @Autowired
    private RedisService redisService;
    /**
     * 保存点赞记录
     *
     * @param userLike 点赞记录
     * @return
     */
    @Override
    public UserLike save(UserLike userLike) {
        return null;
    }

    /**
     * 批量保存或者修改
     *
     * @param likeList 点赞列表
     * @return
     */
    @Override
    public List<UserLike> saveAll(List<UserLike> likeList) {
        return null;
    }

    /**
     * 根据点赞用户ID获取点赞列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pagesize 页容量
     * @return
     */
    @Override
    public PageResult<UserLike> getLikedListByLikedUserId(String userId, Long page, Long pagesize) {
        return null;
    }

    /**
     * 根据用户ID 和 目标ID 查询是否存在点赞记录
     *
     * @param likeUserId 点赞用户ID
     * @param targetId   点赞目标ID
     * @return
     */
    @Override
    public UserLike getByLikeUserIdAndMomentId(String likeUserId, String targetId) {
        return null;
    }

    /**
     * 持久化点赞状态到数据库中
     */
    @Override
    public void transLikedFromRedis2DB() {

    }

    /**
     * 持久化点赞数量到数据库中
     */
    @Override
    public void transLikedCountFromRedis2DB() {

    }
}
