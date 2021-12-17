package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitor;
import io.netty.util.internal.StringUtil;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */
@Service
public class VisitorApiImpl implements VisitorApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询来访记录
     *
     * @param userId
     * @param lastTime
     * @return
     */
    @Override
    public List<Visitor> queryVisitor(Long userId, String lastTime) {
        Query query = new Query(Criteria.where("userId").is(userId));

        if (!StringUtils.isEmpty(lastTime)) {
            // 转换类型
            Long date = Long.valueOf(lastTime);
            // date>=记录的时间
            query.addCriteria(Criteria.where("date").gte(date));
        }
        query.limit(5).with(Sort.by(Sort.Order.desc("date")));

        List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
        //查看是否在推荐用户表中，如果在，则赋予推荐表中的score，如果不在则默认复制
        for (Visitor visitor : visitorList) {

            Long visitorId = visitor.getVisitorUserId();

            Query recommendQuery = new Query();
            recommendQuery.addCriteria(Criteria.where("toUserId").is(userId).and("userId").is(visitorId));

            RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);

            if (null != recommendUser) {
                visitor.setScore(recommendUser.getScore());
            }
            visitor.setScore(75D);
        }
        return visitorList;
    }

    @Override
    public void save(Visitor visitor) {
        mongoTemplate.insert(visitor);
    }
}
