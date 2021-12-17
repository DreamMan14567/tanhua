package com.tanhua.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Moment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.MomentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/16
 * @Description
 */
@Service
@Slf4j
public class CommentService {
    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private MomentApi momentApi;

    @Reference
    private CommentApi commentApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 动态点赞
     *
     * @param publishId
     * @return
     */
    public Long like(String publishId) {
        Long userId = UserHolder.getId();
        String key = "publish_like_" + userId + "_" + publishId;

        String isLiked = (String) redisTemplate.opsForValue().get(key);
        // 如果不为空，则只需要查询即可，不需要再添加评论记录和修改点赞数
        if (!StringUtils.isEmpty(isLiked)) {
            Moment moment = momentApi.findById(publishId);
            return Long.valueOf(moment.getLikeCount());
        }
        // 插入并更新一条点赞记录--评论
        Comment comment = new Comment();
        comment.setTargetId(new ObjectId(publishId));
        comment.setUserId(userId);
        comment.setCommentType(1);
        comment.setTargetType(1);
        // 对点赞记录进行保存
        Long likeCount = commentApi.save(comment);
        // 保存到key中
        redisTemplate.opsForValue().set(key, "1");
        return likeCount;
    }

    public Long dislike(String publishId) {
        Long userId = UserHolder.getId();
        String key = "publish_like_" + userId + "_" + publishId;
        String isLiked = (String) redisTemplate.opsForValue().get(key);
        // 判断redis中是否存在
        if (StringUtils.isNotEmpty(isLiked)) {
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setTargetId(new ObjectId(publishId));
            comment.setCommentType(1);
            comment.setTargetType(1);
            Long likeCount = commentApi.remove(comment);
            redisTemplate.delete(key);
            return likeCount;
        }
        // 如果为空，则表示还没点赞，直接返回即可
        Moment moment = momentApi.findById(publishId);
        return Long.valueOf(moment.getLikeCount());
    }

    public Long love(String publishId) {
        Long userId = UserHolder.getId();
        String key = "publish_love_" + userId + "_" + publishId;

        String isLoved = (String) redisTemplate.opsForValue().get(key);
        // 如果不为空，则只需要查询即可，不需要再添加评论记录和修改点赞数
        if (!StringUtils.isEmpty(isLoved)) {
            Moment moment = momentApi.findById(publishId);
            return Long.valueOf(moment.getLoveCount());
        }
        // 插入并更新一条点赞记录--评论
        Comment comment = new Comment();
        comment.setTargetId(new ObjectId(publishId));
        comment.setUserId(userId);
        comment.setCommentType(3);
        comment.setTargetType(1);
        // 对点赞记录进行保存
        Long loveCount = commentApi.save(comment);
        // 保存到key中
        redisTemplate.opsForValue().set(key, "1");
        return loveCount;
    }

    public Long unlove(String publishId) {
        Long userId = UserHolder.getId();
        String key = "publish_love_" + userId + "_" + publishId;
        String isLoved = (String) redisTemplate.opsForValue().get(key);
        // 判断redis中是否存在
        if (StringUtils.isNotEmpty(isLoved)) {
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setTargetId(new ObjectId(publishId));
            comment.setCommentType(3);
            comment.setTargetType(1);
            Long loveCount = commentApi.remove(comment);
            redisTemplate.delete(key);
            return loveCount;
        }
        // 如果为空，则表示还没点赞，直接返回即可
        Moment moment = momentApi.findById(publishId);
        return Long.valueOf(moment.getLoveCount());
    }

    public PageResult<CommentVo>
    findPage(String movementId, Long page, Long pagesize) {
        // 获取评论传输数据列表
        PageResult pageResult = commentApi.findPage(page, pagesize, movementId);
        // 获取对应的数据集
        List<Comment> commentList = (List<Comment>) pageResult.getItems();

        if (CollectionUtils.isNotEmpty(commentList)) {
            List<Long> commentIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());
            //通过id获取用户详情结果集
            List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(commentIds);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userinfo -> userinfo));

            // 对数据进行封装
            List<CommentVo> voList = commentList.stream().map(comment -> {
                CommentVo vo = new CommentVo();
                UserInfo userInfo = userInfoMap.get(comment.getUserId());
                System.out.println(userInfo);
                BeanUtils.copyProperties(userInfo, vo);
                vo.setId(comment.getId().toHexString());
                BeanUtils.copyProperties(comment, vo);
                vo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
                //设置是否点赞过
                vo.setHasLiked(0);
                String key = "comment_like_" + UserHolder.getId()+"_" + vo.getId();
                if(redisTemplate.hasKey(key)){
                    vo.setHasLiked(1); // 点赞过了
                }
                return vo;
            }).collect(Collectors.toList());

            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 发表评论
     *
     * @param param
     */
    public void postComment(Map<String, String> param) {
        String movementId = param.get("movementId");
        String comment = param.get("comment");
        Long loginUserId = UserHolder.getId();

        Comment myComment = new Comment();
        myComment.setTargetType(1);
        myComment.setCommentType(2);
        myComment.setTargetUserId(loginUserId);
        myComment.setContent(comment);
        myComment.setUserId(UserHolder.getId());
        myComment.setTargetId(new ObjectId(movementId));

        commentApi.addComment(myComment);
    }

    public Long likeComment(String commentId) {
        Long userId = UserHolder.getId();
        String key = "comment_like_" + userId + "_" + commentId;

        // 如果不为空，则只需要查询即可，不需要再添加评论记录和修改点赞数
        if (redisTemplate.hasKey(key)) {
            log.info("key:{}","true");
            Comment comment = commentApi.findById(commentId);
            return Long.valueOf(comment.getLikeCount());
        }
        // 插入并更新一条点赞记录--评论
        Comment comment = new Comment();
        comment.setTargetId(new ObjectId(commentId));
        comment.setUserId(userId);
        comment.setCommentType(1);
        comment.setTargetType(3);
        // 对点赞记录进行保存
        Long loveCount = commentApi.likeComment(comment);
        // 保存到key中
        redisTemplate.opsForValue().set(key, "1");
        return loveCount;
    }

    /**
     * 取消对评论的点赞
     * @param commentId
     * @return
     */
    public Long dislikeCount(String commentId) {
        Long userId = UserHolder.getId();
        String key = "comment_like_" + userId + "_" + commentId;
        // 判断redis中是否存在 已经点赞
        if (redisTemplate.hasKey(key)) {
            log.info("key:{}","true");
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setTargetId(new ObjectId(commentId));
            comment.setCommentType(1);
            comment.setTargetType(3);
            Long likeCount = commentApi.dislikeComment(comment);
            redisTemplate.delete(key);
            return likeCount;
        }
        // 如果为空，则表示还没点赞，直接返回即可
        Comment comment = commentApi.findById(commentId);
        return Long.valueOf(comment.getLikeCount());
    }
}
