package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.dubbo.utils.IdService;
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
 * @date 2021/5/16
 * @Description
 */
@Service
public class VideoApiImpl implements VideoApi {
    @Autowired
    private IdService idService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(Video video) {
        video.setId(ObjectId.get());
        video.setCreated(System.currentTimeMillis());
        video.setVid(idService.nextId("video"));
        mongoTemplate.insert(video);
    }

    @Override
    public PageResult findPage(Long page, Long pagesize) {
        // 设置条件进行筛选
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("created")));
        query.skip((page - 1) * pagesize).limit(pagesize.intValue());

        Long count = mongoTemplate.count(query, Video.class);

        Long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<Video> videoList = new ArrayList<>();

        // 初始化分页对象
        PageResult<Video> pageResult = new PageResult<>(count, pagesize, pages, page, videoList);

        if (count > 0) {
            pageResult.setItems(videoList);
        }

        // 获取所有的视频集合
        return pageResult;
    }

    /**
     * 添加用户
     *
     * @param followUser
     */
    @Override
    public void follow(FollowUser followUser) {
        followUser.setCreated(System.currentTimeMillis());
        followUser.setId(ObjectId.get());
        mongoTemplate.insert(followUser);
    }

    /**
     * 取消关注用户
     *
     * @param followUser
     */
    @Override
    public void cancelFollow(FollowUser followUser) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(followUser.getUserId()).and("followUserId").is(followUser.getFollowUserId()));
        mongoTemplate.remove(query);
    }

    /**
     * 查找指定用户的视频列表
     *
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    @Override
    public PageResult<Video> findUserVideo(Long page, Long pagesize, Long uid) {
        // 设置条件进行筛选
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(uid));
        query.with(Sort.by(Sort.Order.desc("created")));
        query.skip((page - 1) * pagesize).limit(pagesize.intValue());

        Long count = mongoTemplate.count(query, Video.class);

        Long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<Video> videoList = new ArrayList<>();

        // 初始化分页对象
        PageResult<Video> pageResult = new PageResult<>(count, pagesize, pages, page, videoList);

        if (count > 0) {
            pageResult.setItems(videoList);
        }

        // 获取所有的视频集合
        return pageResult;
    }
}
