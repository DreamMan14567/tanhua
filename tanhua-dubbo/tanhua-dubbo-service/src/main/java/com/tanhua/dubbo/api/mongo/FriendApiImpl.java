package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 分页获取好友列表
     *
     * @param page
     * @param pagesize
     * @param loginUserId
     * @return
     */
    @Override
    public PageResult findPage(Long page, Long pagesize, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        long count = mongoTemplate.count(query, Friend.class);

        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;
        List<Friend> friendList = new ArrayList<>();
        PageResult pageResult = new PageResult(count, pagesize, pages, page, friendList);

        if (count > 0) {
            query.skip((page - 1) * pagesize).limit(pagesize.intValue());
            friendList = mongoTemplate.find(query, Friend.class);
            pageResult.setItems(friendList);
        }
        log.info("pageresult:{}",pageResult);
        return pageResult;
    }
}
