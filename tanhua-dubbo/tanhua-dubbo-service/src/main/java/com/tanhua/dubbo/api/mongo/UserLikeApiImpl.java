package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */
@Service
public class UserLikeApiImpl implements UserLikeApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Long countLoveEach(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, Friend.class);
    }

    @Override
    public Long countLove(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, UserLike.class);
    }

    @Override
    public Long countFans(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("friendId").is(userId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 查询互相喜欢列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    @Override
    public PageResult<RecommendUser> findPage4EachLike(Long page, Long pagesize, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        // 查找好友表
        long count = mongoTemplate.count(query, Friend.class);//9
        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<RecommendUser> recommendUserList = new ArrayList<>();

        PageResult pageResult = new PageResult(count, pagesize, pages, page, recommendUserList);

        if (count > 0) {
            query.skip((page - 1) * pagesize).limit(pagesize.intValue());
            List<Friend> friendList = mongoTemplate.find(query, Friend.class);
            recommendUserList = new ArrayList<>();
            for (Friend friend : friendList) {
                Long toUserId = friend.getFriendId();
                RecommendUser recommendUser = queryScore(toUserId, loginUserId);
                // 获取推荐对象添加到列表中
                recommendUserList.add(recommendUser);
            }
            pageResult.setItems(recommendUserList);
        }
        return pageResult;
    }

    /**
     * 查询单相思列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    @Override
    public PageResult<RecommendUser> findPage4Like(Long page, Long pagesize, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        long count = mongoTemplate.count(query, Friend.class);
        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<RecommendUser> recommendUserList = new ArrayList<>();

        PageResult pageResult = new PageResult(count, pagesize, pages, page, recommendUserList);

        if (count > 0) {
            query.skip((page - 1) * pagesize).limit(pagesize.intValue()).with(Sort.by(Sort.Order.desc("created")));
            List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);

            for (UserLike userLike : userLikeList) {
                Long toUserId = userLike.getLikeUserId();
                RecommendUser recommendUser = queryScore(toUserId, loginUserId);
                recommendUserList.add(recommendUser);
            }
            pageResult.setItems(recommendUserList);
        }

        return pageResult;
    }

    /**
     * 查询粉丝列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    @Override
    public PageResult<RecommendUser> findPage4Fans(Long page, Long pagesize, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId));

        long count = mongoTemplate.count(query, Friend.class);
        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<RecommendUser> recommendUserList = new ArrayList<>();

        PageResult pageResult = new PageResult(count, pagesize, pages, page, recommendUserList);

        if (count > 0) {
            // 分页
            query.skip((page - 1) * pagesize).limit(pagesize.intValue()).with(Sort.by(Sort.Order.desc("created")));
            // 查找喜欢用户
            List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
            // 遍历装配
            for (UserLike userLike : userLikeList) {
                long toUserId = userLike.getUserId();
                RecommendUser recommendUser = queryScore(toUserId, loginUserId);
                recommendUserList.add(recommendUser);
            }
            pageResult.setItems(recommendUserList);
        }
        return pageResult;
    }

    /**
     * 查询我从你的世界走过列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    @Override
    public PageResult<RecommendUser> findPage4Watch(Long page, Long pagesize, Long loginUserId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        long count = mongoTemplate.count(query, Visitor.class);
        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<RecommendUser> recommendUserList = new ArrayList<>();

        PageResult pageResult = new PageResult(count, pagesize, pages, page, recommendUserList);

        if (count > 0) {
            query.skip((page - 1) * pagesize).limit(pagesize.intValue()).with(Sort.by(Sort.Order.desc("created")));
            List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);

            for (Visitor visitor : visitorList) {
                long userId = visitor.getUserId();
                long toUserId = visitor.getVisitorUserId();
                RecommendUser recommendUser = queryScore(toUserId, userId);
                recommendUserList.add(recommendUser);
            }
            pageResult.setItems(recommendUserList);
        }
        return pageResult;
    }

    /**
     * 关注用户
     *
     * @param userId
     * @param loginUserId
     */
    @Override
    public boolean fansLike(Long userId, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId));

        boolean isLike = false;
        // 判断关注用户是否关注登录用户
        if (mongoTemplate.exists(query, UserLike.class)) {
            // 如果是则添加为好友
            // 从喜欢表中删除该记录
            mongoTemplate.remove(query,UserLike.class);
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

            isLike = true;

        }else {
            // 如果是单相思
            UserLike userLike = new UserLike();
            userLike.setUserId(loginUserId);
            userLike.setLikeUserId(userId);
            userLike.setCreated(System.currentTimeMillis());
            mongoTemplate.insert(userLike);
        }
        return isLike;
    }


    /**
     * 获取好友推荐 主要是查询分数
     *
     * @param userId
     * @param toUserId
     * @return
     */
    public RecommendUser queryScore(Long toUserId, Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("toUserId").is(userId)
                .and("userId").is(toUserId));
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);

        // 给访客设置缘分值
        if (null == recommendUser) {
            recommendUser = new RecommendUser();
            recommendUser.setUserId(toUserId);
            recommendUser.setToUserId(userId);
            recommendUser.setScore(70d);
        }
        return recommendUser;
    }
}
