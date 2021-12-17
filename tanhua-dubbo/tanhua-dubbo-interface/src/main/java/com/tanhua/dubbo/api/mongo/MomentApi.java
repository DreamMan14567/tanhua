package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Moment;
import com.tanhua.domain.vo.PageResult;
import org.bson.types.ObjectId;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
public interface MomentApi {
    /**
     * 添加动态
     *
     * @param moment
     */
    void add(Moment moment);

    /**
     * 分页查看好友动态
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    PageResult queryFriendMoment(Long page, Long pagesize, Long loginUserId);

    /**
     * 获取推荐给登录用户的动态
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    PageResult queryRecommendMoment(Long page, Long pagesize, Long loginUserId);

    /**
     * 查询用户动态
     * @param page 页码
     * @param pagesize 页面大小
     * @param userId 用户ID
     * @return
     */
    PageResult queryUserMoment(Long page, Long pagesize, Long userId);

    Moment findById(String publishId);

    PageResult<Moment> queryUserStateMoment(Long page, Long pagesize, Long uid, int state);
}
