package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
public interface RecommendUserApi {
    /**
     * 查询缘分值最高的人
     *
     * @param userId
     * @return
     */
    RecommendUser queryBestByScore(Long userId);

    /**
     * 分页降序查询推荐用户
     *
     * @param page
     * @param pagesize
     * @return
     */
    PageResult queryRecommendUser(Long page, Long pagesize, Long userId);

    /**
     * 查找佳人信息
     *
     * @param userId
     * @return
     */
    RecommendUser queryBestInfo(Long userId, Long loginUserId);

    /**
     * 判断我喜欢的人是否已经喜欢我
     *
     * @param userId
     * @param loginUserId
     * @return
     */
    Boolean isLoveMe(Long userId, Long loginUserId);

    /**
     * 进行互相喜欢添加
     *
     * @param loginUserId
     * @param userId
     */
    void loveEachOther(Long loginUserId, Long userId);

    /**
     * 仅仅是我喜欢他
     *
     * @param loginUserId
     * @param userId
     */
    void justIlove(Long loginUserId, Long userId);

    /**
     * 寻找是否存在我喜欢的人
     *
     * @return
     */
    boolean findMyLove(Long userId, Long loginUserId);

    /**
     * 寻找我讨厌的人
     *
     * @return
     */
    boolean findMyHate(Long userId, Long loginUserId);

    /**
     * 根据ID查找所有的推荐人
     *
     * @param loginUserId
     * @return
     */
    List<RecommendUser> queryRecommendUserList(Long loginUserId);

    /**
     * 添加到黑名单
     *
     * @param loginUserId
     */
    void addBlacklist(Long loginUserId,Long uid);
}
