package com.tanhua.dubbo.api.mongo;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.Moment;
import com.tanhua.domain.mongo.RecommendQuanzi;
import com.tanhua.domain.mongo.TimeLine;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.utils.IdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */

@Service
@Slf4j
public class MomentApiImpl implements MomentApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    /**
     * 添加动态
     *
     * @param moment
     */
    @Override
    public void add(Moment moment) {
        String collName = "quanzi_time_line_";
        long timeMills = System.currentTimeMillis();
        //添加一条动态需要为每一个时间线表添加一条时间线，到时候查询分页的时候可以更加快捷方便
        Long userId = moment.getUserId();
        // 通过动态获取用户ID，接着查询出所有的好友
        Query query = new Query(Criteria.where("userId").is(userId));
        moment.setCreated(System.currentTimeMillis());
        moment.setId(new ObjectId());
        moment.setPid(idService.nextId("quanzi_publish"));
        // 添加动态到总表中
        mongoTemplate.insert(moment);

        // 添加进自己的时间线中
        TimeLine timeLine = new TimeLine();
        timeLine.setPublishId(moment.getId());
        timeLine.setUserId(userId);
        timeLine.setCreated(timeMills);
        mongoTemplate.insert(timeLine, collName + userId);
        // 获取所有的好友
        List<Friend> friends = mongoTemplate.find(query, Friend.class);
        // 通过friends可以获取所有的用户ID
        List<Long> ids = friends.stream().map(Friend::getUserId).collect(Collectors.toList());

        // 通过好友ID往每一个好友的时间线表中添加一条时间线
        for (Long id : ids) {
            timeLine = new TimeLine();
            timeLine.setCreated(timeMills);
            timeLine.setUserId(id);
            timeLine.setPublishId(moment.getId());
            mongoTemplate.insert(timeLine, collName + id);
        }
        // todo return moment.getId();

    }

    /**
     * 分页查看好友动态
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    @Override
    public PageResult queryFriendMoment(Long page, Long pagesize, Long loginUserId) {
        // 分页降序查询好友动态
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        String collName = "quanzi_time_line_" + loginUserId;
        // 查询记录总数
        long count = mongoTemplate.count(query, collName);
        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        // 构建分页对象
        List<Moment> momentList = new ArrayList<>();
        PageResult pageResult = new PageResult(count, pagesize, pages, page, momentList);

        // 记录大于O才继续进行
        if (count > 0) {
            query.skip((page - 1) * pagesize).limit(pagesize.intValue());
            query.with(Sort.by(Sort.Order.desc("created")));
            // 获取对应时间线的列表
            List<TimeLine> timelineList = mongoTemplate.find(query, TimeLine.class, collName);
            // 通过时间线表获取动态ids
            List<ObjectId> objectIds = timelineList.stream().map(TimeLine::getPublishId).collect(Collectors.toList());
            Query momentQuery = new Query(Criteria.where("targetId").in(objectIds));
            momentQuery.with(Sort.by(Sort.Order.desc("created")));
            //查询对应所有动态
            momentList = mongoTemplate.find(query, Moment.class);
            log.info("momentList:{}", momentList);
            pageResult.setItems(momentList);
        }
        // 返回结果即可
        return pageResult;
    }

    /**
     * 获取推荐给登录用户的动态
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    @Override
    public PageResult queryRecommendMoment(Long page, Long pagesize, Long loginUserId) {
        // 通过loginUserId查询推荐动态表
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        query.skip(pagesize * (page - 1)).limit(pagesize.intValue());
        //获取count
        long count = mongoTemplate.count(query, RecommendQuanzi.class);

        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<RecommendQuanzi> recommendQuanzis = mongoTemplate.find(query, RecommendQuanzi.class);
        List<Moment> momentList = new ArrayList<>();
        PageResult<Moment> pageResult = new PageResult<>(count, pagesize, pages, page, momentList);
        if (CollectionUtils.isNotEmpty(recommendQuanzis)) {
            // 通过推荐动态获取所有的动态ID
            List<ObjectId> momentIds = recommendQuanzis.stream().map(RecommendQuanzi::getPublishId).collect(Collectors.toList());
            // 通过动态ID就可以分页获取动态详情
            Query momentQuery = new Query(Criteria.where("id").in(momentIds));
            momentQuery.skip(pagesize * (page - 1)).limit(pagesize.intValue()).with(Sort.by(Sort.Order.desc("created")));
            momentList = mongoTemplate.find(momentQuery, Moment.class);
            pageResult.setItems(momentList);
        }

        return pageResult;

    }

    /**
     * 查询用户动态
     *
     * @param page     页码
     * @param pagesize 页面大小
     * @param userId   用户ID
     * @return
     */
    @Override
    public PageResult queryUserMoment(Long page, Long pagesize, Long userId) {
        // select * from quanzi_time_line_num where userId = userId;
        String collName = "quanzi_time_line_" + userId;
        Query query = new Query(Criteria.where("userId").is(userId));
        // 为page获取count
        long counts = mongoTemplate.count(query, collName);
        //设置pages
        long pages = counts / pagesize;
        pages += counts % pagesize == 0 ? 0 : 1;
        // 构建pageResult对象进行封装
        List<Moment> momentList = new ArrayList<>();
        PageResult<Moment> pageResult = new PageResult<>(counts, pagesize, pages, page, momentList);
        // 判断是否有记录
        if (counts > 0) {
            // 获取时间线表
            List<TimeLine> timelineList = mongoTemplate.find(query, TimeLine.class, collName);

            // 通过时间线表获取动态ID，进而获取所有动态
            List<ObjectId> momentIds = timelineList.stream().map(TimeLine::getPublishId).collect(Collectors.toList());
            Query momentQuery = new Query(Criteria.where("id").in(momentIds));
            momentQuery.skip(pagesize * (page - 1)).limit(pagesize.intValue()).with(Sort.by(Sort.Order.desc("created")));
            momentList = mongoTemplate.find(momentQuery, Moment.class);
            pageResult.setItems(momentList);
        }
        return pageResult;
    }

    @Override
    public Moment findById(String publishId) {
        return mongoTemplate.findById(new ObjectId(publishId), Moment.class);
    }

    /**
     * 查找指定状态的动态
     *
     * @param page
     * @param pagesize
     * @param uid
     * @param state
     * @return
     */
    @Override
    public PageResult<Moment> queryUserStateMoment(Long page, Long pagesize, Long uid, int state) {
        // select * from quanzi_time_line_num where userId = userId;
        String collName = "quanzi_time_line_" + uid;
        Query query = new Query(Criteria.where("userId").is(uid));
        // 为page获取count
        long counts = mongoTemplate.count(query, collName);
        //设置pages
        long pages = counts / pagesize;
        pages += counts % pagesize == 0 ? 0 : 1;
        // 构建pageResult对象进行封装
        List<Moment> momentList = new ArrayList<>();
        PageResult<Moment> pageResult = new PageResult<>(counts, pagesize, pages, page, momentList);
        // 判断是否有记录
        if (counts > 0) {
            // 获取时间线表
            List<TimeLine> timelineList = mongoTemplate.find(query, TimeLine.class, collName);

            // 通过时间线表获取动态ID，进而获取所有动态
            List<ObjectId> momentIds = timelineList.stream().map(TimeLine::getPublishId).collect(Collectors.toList());
            Query momentQuery = new Query(Criteria.where("id").in(momentIds));
            momentQuery.skip(pagesize * (page - 1)).limit(pagesize.intValue()).with(Sort.by(Sort.Order.desc("created")));
            momentList = mongoTemplate.find(momentQuery, Moment.class);
            pageResult.setItems(momentList);
        }
        return pageResult;
    }


}

