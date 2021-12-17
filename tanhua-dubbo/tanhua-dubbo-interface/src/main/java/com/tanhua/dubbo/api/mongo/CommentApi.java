package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

public interface CommentApi {
    /**
     * 添加评论记录
     *
     * @param comment
     * @return
     */
    Long save(Comment comment);

    /**
     * 删除评论记录
     *
     * @param comment
     * @return
     */
    Long remove(Comment comment);

    /**
     * 分页查找comment
     *
     * @param page
     * @param pagesize
     * @param movementId
     * @return
     */
    PageResult findPage(Long page, Long pagesize, String movementId);

    /**
     * 发表评论
     *
     * @param myComment
     */
    void addComment(Comment myComment);

    /**
     * 查找评论根据ID
     *
     * @param commentId
     * @return
     */
    Comment findById(String commentId);

    /**
     * 点赞评论
     *
     * @param comment
     * @return
     */
    Long likeComment(Comment comment);

    /**
     * 取消点赞评论
     *
     * @param comment
     * @return
     */
    Long dislikeComment(Comment comment);

    /**
     * 根据用户ID查找评论列表
     *
     * @param page
     * @param pagesize
     * @param type
     * @param userId
     * @return
     */
    PageResult findByUserId(Long page, Long pagesize, Integer type, Long userId);
}
