package com.tanhua.dubbo.api.mongo;


import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */

public interface UserLikeApi {
    /**
     * 互相喜欢
     *
     * @param userId
     * @return
     */
    Long countLoveEach(Long userId);

    /**
     * 喜欢
     *
     * @param userId
     * @return
     */
    Long countLove(Long userId);

    /**
     * 粉丝
     *
     * @param userId
     * @return
     */
    Long countFans(Long userId);

    /**
     * 查询互相喜欢列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    PageResult<RecommendUser> findPage4EachLike(Long page, Long pagesize, Long loginUserId);

    /**
     * 查询单相思列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    PageResult<RecommendUser> findPage4Like(Long page, Long pagesize, Long loginUserId);

    /**
     * 查询粉丝列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    PageResult<RecommendUser> findPage4Fans(Long page, Long pagesize, Long loginUserId);

    /**
     * 查询我从你的世界走过列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    PageResult<RecommendUser> findPage4Watch(Long page, Long pagesize, Long loginUserId);

    /**
     * 关注用户
     *
     * @param userId
     * @param loginUserId
     */
    boolean fansLike(Long userId, Long loginUserId);

}
