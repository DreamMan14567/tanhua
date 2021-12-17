package com.tanhua.service;

import com.tanhua.domain.mongo.UserLike;
import com.tanhua.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞 状态为 1
     *
     * @param likeUserId
     * @param targetId
     */
    @Override
    public void saveLike2Redis(String likeUserId, String targetId) {
        String key = RedisUtil.getLikedKey(likeUserId, targetId);
        redisTemplate.opsForHash().put(RedisUtil.MAP_KEY_USER_LIKED, key, 1);
    }

    /**
     * 取消点赞 状态为0
     *
     * @param likeUserId
     * @param targetId
     */
    @Override
    public void unlikeFromRedis(String likeUserId, String targetId) {
        String key = RedisUtil.getLikedKey(likeUserId, targetId);
        //设置当前用户点赞状态
        redisTemplate.opsForHash().put(RedisUtil.MAP_KEY_USER_LIKED, key, 0);
    }

    /**
     * 从redis中删除一条点赞数据
     *
     * @param likeUserId
     * @param targetId
     */
    @Override
    public void deleteLikeFromRedis(String likeUserId, String targetId) {
        String key = RedisUtil.getLikedKey(likeUserId, targetId);
        redisTemplate.opsForHash().delete(RedisUtil.MAP_KEY_USER_LIKED, key);
    }

    /**
     * redis中点赞记录+1
     *
     * @param likeUserId
     * @param targetId
     */
    @Override
    public void incrementLikeCount(String likeUserId, String targetId) {
        String key = RedisUtil.getLikedKey(likeUserId, targetId);
        redisTemplate.opsForHash().increment(RedisUtil.MAP_KEY_USER_LIED_COUNT, key, 1);
    }

    /**
     * redis中点赞记录-1
     *
     * @param likeUserId
     * @param targetId
     */
    @Override
    public void decrementLikeCount(String likeUserId, String targetId) {
        String key = RedisUtil.getLikedKey(likeUserId, targetId);
        redisTemplate.opsForHash().increment(RedisUtil.MAP_KEY_USER_LIED_COUNT, key, -1);
    }

    /**
     * 获取redis中存储的点赞记录
     *
     * @return
     */
    @Override
    public List<UserLike> getLikeDataFromRedis() {
        // 先从redis缓存中进行获取
        // 如果redis中没有缓存点赞的记录 那么就从mongodb数据库中获取
        // 查询对应的动态的所有点赞数进行展示
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisUtil.MAP_KEY_USER_LIKED, ScanOptions.NONE);
        List<UserLike> list = new ArrayList<>();

        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();
            String key = (String) entry.getKey();
            // 分离出likeUserId,likedPostId
            String[] split = key.split("::");
            String userId = split[0];
            String targetId = split[1];
            Integer value = (Integer) entry.getValue();

            //组装 momentLike 对象 添加点赞记录 userId,targetId
            UserLike userLike = new UserLike();
            //添加到列表
            list.add(userLike);
        }
        return list;
    }

    /**
     * 获取redis中的点赞数
     *
     * @return
     */
    @Override
    public List<Long> getLikeCountFromRedis() {
        return null;
    }
}
