package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Moment;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.utils.IdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class CommentApiImpl implements CommentApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;


    /**
     * 点赞
     *
     * @param comment
     * @return
     */
    @Override
    public Long save(Comment comment) {
        ObjectId targetId = comment.getTargetId();
        // 需要添油加醋
        comment.setCreated(System.currentTimeMillis());
        comment.setTargetUserId(comment.getTargetUserId());
        // 进行插入
        mongoTemplate.save(comment);
        // 修改更新值 条件：
        Query query = new Query(Criteria.where("id").is(targetId));
        Update update = new Update();
        update.inc(comment.getCol(), 1);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        // 返回最新值
        Moment newMoment = mongoTemplate.findAndModify(query, update, options, Moment.class);

        return getCount(comment, newMoment);
    }

    /**
     * 删除评论记录
     *
     * @param comment
     * @return
     */
    @Override
    public Long remove(Comment comment) {
        ObjectId targetId = comment.getTargetId();
        // 删除该记录
        Query queryRemove = new Query();
        queryRemove.addCriteria(Criteria.where("userId").is(comment.getUserId())
                .and("commentType").is(1)
                .and("targetType").is(1));
        mongoTemplate.remove(queryRemove, Comment.class);
        // 对likeCount进行-1
        Query query = new Query(Criteria.where("id").is(targetId));

        Update update = new Update();
        update.inc(comment.getCol(), -1);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);

        Moment newMoment = mongoTemplate.findAndModify(query, update, options, Moment.class);

        return getCount(comment, newMoment);
    }

    /**
     * 分页查找comment
     *
     * @param page
     * @param pagesize
     * @param movementId
     * @return
     */
    @Override
    public PageResult findPage(Long page, Long pagesize, String movementId) {
        Query query = new Query(Criteria.where("targetId").is(new ObjectId(movementId)));
        query.addCriteria(Criteria.where("commentType").is(2).and("targetType").is(1));
        query.skip(pagesize * (page - 1)).limit(pagesize.intValue());
        query.with(Sort.by(Sort.Order.desc("created")));

        Long count = mongoTemplate.count(query, Comment.class);

        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<Comment> commentList = new ArrayList<>();
        PageResult<Comment> pageResult = new PageResult<>(count, pagesize, pages, page, commentList);

        if (count > 0) {
            commentList = mongoTemplate.find(query, Comment.class);
            pageResult.setItems(commentList);
        }
        log.info("pageresult:{}",pageResult);
        return pageResult;
    }

    /**
     * 发表评论
     *
     * @param myComment
     */
    @Override
    public void addComment(Comment myComment) {
        myComment.setCreated(System.currentTimeMillis());
        mongoTemplate.insert(myComment);
    }

    /**
     * 查找评论根据ID
     *
     * @param commentId
     * @return
     */
    @Override
    public Comment findById(String commentId) {
        return mongoTemplate.findById(new ObjectId(commentId), Comment.class);
    }

    /**
     * 点赞评论
     *
     * @param comment
     * @return
     */
    @Override
    public Long likeComment(Comment comment) {
        // 获取评论的点赞评论的ID
        ObjectId targetId = comment.getTargetId();
        // 通过点赞评论的ID可以获取原本的Comment
        Comment commentOrigin = mongoTemplate.findById(targetId, Comment.class);
        // 补充comment的信息 targetUserId,created
        comment.setCreated(System.currentTimeMillis());
        assert commentOrigin != null;
        comment.setTargetUserId(commentOrigin.getTargetUserId());

        mongoTemplate.insert(comment);
        //进行更新返回值
        Query query = new Query(Criteria.where("id").is(targetId));

        Update update = new Update();
        update.inc("likeCount", 1);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        options.upsert(true);

        Comment newComment = mongoTemplate.findAndModify(query, update, options, Comment.class);
        assert newComment != null;
        return Long.valueOf(newComment.getLikeCount());
    }

    /**
     * 取消点赞评论
     *
     * @param comment
     * @return
     */
    @Override
    public Long dislikeComment(Comment comment) {
        // 删除评论表数据
        Query commentQuery = new Query();
        commentQuery.addCriteria(Criteria.where("targetId").is(comment.getTargetId())
                .and("userId").is(comment.getUserId())
                .and("commentType").is(comment.getCommentType()));
        // delete from quanzi_comment where userId=? targetId=? commentType=?
        mongoTemplate.remove(commentQuery, Comment.class);

        //进行更新返回值
        Query query = new Query(Criteria.where("id").is(comment.getTargetId()));

        Update update = new Update();
        update.inc("likeCount", -1);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        options.upsert(true);

        Comment newComment = mongoTemplate.findAndModify(query, update, options, Comment.class);
        assert newComment != null;
        return Long.valueOf(newComment.getLikeCount());
    }

    /**
     * 根据用户ID查找评论列表
     *
     * @param page
     * @param pagesize
     * @param type
     * @param userId   被评论用户
     * @return
     */
    @Override
    public PageResult findByUserId(Long page, Long pagesize, Integer type, Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId)
                .and("commentType").is(type));
        long count = mongoTemplate.count(query, Comment.class);
        long pages = count / pagesize;
        pages += count % pagesize == 0 ? 0 : 1;

        List<Comment> commentList = new ArrayList<>();

        PageResult pageResult = new PageResult(count,pagesize,pages,page,commentList);

        if (count > 0) {
            query.limit(pagesize.intValue()).skip(pagesize * (page - 1)).with(Sort.by(Sort.Order.desc("created")));
            commentList = mongoTemplate.find(query, Comment.class);
            pageResult.setItems(commentList);
        }
        return pageResult;
    }

    /**
     * 通过 评论的类型，获取相应的数量
     *
     * @param comment
     * @param newMoment
     * @return
     */
    private long getCount(Comment comment, Moment newMoment) {
        //评论类型，1-点赞，2-评论，3-喜欢
        switch (comment.getCommentType()) {
            case 1:
                return newMoment.getLikeCount();
            case 2:
                return newMoment.getCommentCount();
            case 3:
                return newMoment.getLoveCount();
            default:
                return newMoment.getLikeCount();
        }
    }
}
