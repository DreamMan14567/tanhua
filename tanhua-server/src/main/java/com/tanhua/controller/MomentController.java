package com.tanhua.controller;

import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.service.CommentService;
import com.tanhua.service.MomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */

@RestController
@RequestMapping("/movements")
public class MomentController {
    @Autowired
    private MomentService momentService;

    @Autowired
    private CommentService commentService;

    /**
     * 发布动态
     *
     * @param publishVo    动态传输对象
     * @param imageContent 动态附属文件
     * @return
     */
    @PostMapping
    public ResponseEntity postMoment(PublishVo publishVo, MultipartFile[] imageContent) {
        momentService.postMoment(publishVo, imageContent);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询好友动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping
    public ResponseEntity queryFriendMoment(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<MomentVo> pageResult = momentService.queryFriendMoment(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 获取推荐动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/recommend")
    public ResponseEntity getRecommendMoment(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<MomentVo> pageResult = momentService.getRecommendMoment(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询用户动态
     *
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity getMyMoment(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize, Long userId) {
        PageResult<MomentVo> pageResult = momentService.getMyMoment(page, pagesize, userId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 点赞动态
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}/like")
    public ResponseEntity like(@PathVariable String publishId) {
        Long likeCount = commentService.like(publishId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消点赞动态
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}/dislike")
    public ResponseEntity dislike(@PathVariable String publishId) {
        Long likeCount = commentService.dislike(publishId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 喜欢
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}/love")
    public ResponseEntity love(@PathVariable String publishId) {
        Long loveCount = commentService.love(publishId);
        return ResponseEntity.ok(loveCount);
    }

    /**
     * 取消喜欢
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}/unlove")
    public ResponseEntity unlove(@PathVariable String publishId) {
        Long loveCount = commentService.unlove(publishId);
        return ResponseEntity.ok(loveCount);
    }

    /**
     * 单条动态查询
     */
    @GetMapping("/{publishId}")
    public ResponseEntity findById(@PathVariable String publishId) {
        // APK会自动发送/movements/visitors， 如果谁看过我的功能没有完成的话，就会被我们这个方式捕获, 所以要先过滤visitors
        // 防止后台报错
        if ("visitors".equals(publishId)) {
            return ResponseEntity.ok(null);
        }
        MomentVo vo = momentService.findById(publishId);
        return ResponseEntity.ok(vo);
    }

    /**
     * 谁看过我
     */
    @GetMapping("/visitors")
    public ResponseEntity visit(){
        List<VisitorVo> voList = momentService.visit();
        return ResponseEntity.ok(voList);
    }

}
