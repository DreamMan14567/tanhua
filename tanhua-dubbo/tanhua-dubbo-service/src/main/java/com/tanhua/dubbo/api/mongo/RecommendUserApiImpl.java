package com.tanhua.dubbo.api.mongo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlackListMapper;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.rmi.server.ObjID;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
@Service
public class RecommendUserApiImpl implements RecommendUserApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BlackListMapper blackListMapper;

    /**
     * 查询缘分值最高的人
     *
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryBestByScore(Long userId) {
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    /**
     * 分页降序查询推荐用户
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<RecommendUser> queryRecommendUser(Long page, Long pagesize, Long userId) {

        Query query = new Query(Criteria.where("toUserId").is(userId));
        // 查询总数
        long count = mongoTemplate.count(query, RecommendUser.class);

        List<RecommendUser> recommendUsers = new ArrayList<>();
        // 如果总数大于0，则代表有记录
        if (count > 0) {
            // 获取所有推荐人信息
            query.limit(pagesize.intValue()).skip(pagesize * (page - 1));
            recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        }
        //补充用户信息
        PageResult<RecommendUser> pageResult = new PageResult<>();
        pageResult.setItems(recommendUsers);
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        pageResult.setCounts(count);
        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;
        pageResult.setPages(pages);

        return pageResult;

    }

    /**
     * 查找佳人信息
     *
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryBestInfo(Long userId, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId)
                .and("toUserId").is(loginUserId));
        query.limit(1);
        query.with(Sort.by(Sort.Order.desc("score")));

        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    /**
     * 判断我喜欢的人是否已经喜欢我
     *
     * @param userId
     * @param loginUserId
     * @return
     */
    @Override
    public Boolean isLoveMe(Long userId, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("likeUserId").is(loginUserId));
        Friend one = mongoTemplate.findOne(query, Friend.class);
        return null != one;
    }

    /**
     * 进行互相喜欢添加
     *
     * @param loginUserId
     * @param userId
     */
    @Override
    public void loveEachOther(Long loginUserId, Long userId) {
        // 先把它喜欢我进行删除
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId));
        // 然后双向添加为好友

        mongoTemplate.remove(query, UserLike.class);
        // 添加新纪录到朋友表中
        Long timeMills = System.currentTimeMillis();
        Friend friend1 = new Friend();
        friend1.setUserId(loginUserId);
        friend1.setFriendId(userId);
        friend1.setCreated(timeMills);
        mongoTemplate.insert(friend1);

        Friend friend2 = new Friend();
        friend2.setUserId(userId);
        friend2.setFriendId(loginUserId);
        friend2.setCreated(timeMills);
        mongoTemplate.insert(friend2);
    }

    /**
     * 仅仅是我喜欢他
     *
     * @param loginUserId
     * @param userId
     */
    @Override
    public void justIlove(Long loginUserId, Long userId) {
        UserLike userLike = new UserLike();
        userLike.setUserId(loginUserId);
        userLike.setLikeUserId(userId);
        userLike.setId(ObjectId.get());
        userLike.setCreated(System.currentTimeMillis());
        mongoTemplate.insert(userLike);
    }

    /**
     * 寻找是否存在我喜欢的人
     *
     * @return
     */
    @Override
    public boolean findMyLove(Long userId, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId).and("likeUserId").is(userId));

        Query friendQuery = new Query();
        friendQuery.addCriteria(Criteria.where("userId").is(loginUserId).and("friendId").is(userId));
        return mongoTemplate.exists(query, UserLike.class) || mongoTemplate.exists(friendQuery, Friend.class);
    }

    /**
     * 寻找我讨厌的人
     *
     * @return
     */
    @Override
    public boolean findMyHate(Long userId, Long loginUserId) {
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUserId);
        queryWrapper.eq("black_user_id", userId);
        return blackListMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 根据ID查找所有的推荐人
     *
     * @param loginUserId
     * @return
     */
    @Override
    public List<RecommendUser> queryRecommendUserList(Long loginUserId) {
        Query query = new Query(Criteria.where("toUserId").is(loginUserId));
        return mongoTemplate.find(query,RecommendUser.class);
    }

    /**
     * 添加到黑名单
     *
     * @param loginUserId
     */
    @Override
    public void addBlacklist(Long loginUserId,Long userId) {
        BlackList blackList = new BlackList();
        blackList.setUserId(loginUserId);
        blackList.setBlackUserId(userId);
        blackList.setCreated(new Date());
        blackList.setUpdated(new Date());
        blackListMapper.insert(blackList);
    }

}
