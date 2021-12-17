package com.tanhua.controller;

import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/16
 * @Description
 */
@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 通过动态id查询评论列表
     *
     * @param movementId
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping
    public ResponseEntity findPage(String movementId,
                                   @RequestParam(defaultValue = "1") Long page,
                                   @RequestParam(defaultValue = "10") Long pagesize) {
        page = page > 0 ? page : 1;
        PageResult<CommentVo> pageResult = commentService.findPage(movementId, page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 发表评论
     *
     * @param param
     * @return
     */
    @PostMapping
    public ResponseEntity postComment(@RequestBody Map<String, String> param) {
        commentService.postComment(param);
        return ResponseEntity.ok(null);
    }

    /**
     * 评论点赞
     * @param commentId
     * @return
     */
    @GetMapping("/{publishId}/like")
    public ResponseEntity commentLike(@PathVariable("publishId") String commentId){
        Long likeCount = commentService.likeComment(commentId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 评论取消点赞
     * @param commentId
     * @return
     */
    @GetMapping("/{publishId}/dislike")
    public ResponseEntity commentDislike(@PathVariable("publishId") String commentId){
        Long likeCount = commentService.dislikeCount(commentId);
        return ResponseEntity.ok(likeCount);
    }

}
