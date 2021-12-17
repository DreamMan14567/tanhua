package com.tanhua.dubbo.api.mongo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.AnnouncementMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
@Service
public class IMApiImpl implements IMApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AnnouncementMapper announcementMapper;
    /**
     * 分页获取公告列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Announcement> queryAnnouncement(Long page, Long pagesize) {
        IPage<Announcement> iPage = new Page<>(page,pagesize);
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();

        IPage<Announcement> announcementIPage = announcementMapper.selectPage(iPage, queryWrapper);

        PageResult<Announcement> pageResult = new PageResult<>(announcementIPage.getTotal(),pagesize,announcementIPage.getPages(),page,announcementIPage.getRecords());

        return pageResult;

    }

    @Override
    public void addFriend(Long loginUserId, Long userId) {
        // 进行查询是否存在该关系，如果不存在，添加到好友表
        Query query1 = new Query();
        query1.addCriteria(Criteria.where("userId").is(loginUserId)
        .and("friendId").is(userId));
        if (!mongoTemplate.exists(query1, Friend.class)) {
            Friend friend = new Friend();
            friend.setCreated(System.currentTimeMillis());
            friend.setFriendId(userId);
            friend.setUserId(loginUserId);
            mongoTemplate.insert(friend);
        }

        Query query2 = new Query();
        query2.addCriteria(Criteria.where("userId").is(loginUserId)
        .and("friendId").is(userId));
        if (!mongoTemplate.exists(query2, Friend.class)) {
            Friend friend = new Friend();
            friend.setCreated(System.currentTimeMillis());
            friend.setFriendId(loginUserId);
            friend.setUserId(userId);
            mongoTemplate.insert(friend);
        }
    }
}
