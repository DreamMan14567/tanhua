package com.tanhua.manage.controller;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Moment;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.manage.domain.MomentDateStrVo;
import com.tanhua.manage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author user_Chubby
 * @date 2021/5/19
 * @Description
 */
@RestController
@RequestMapping("/manage")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 所有用户信息分页显示
     *
     * @return
     */
    @GetMapping("/users")
    public ResponseEntity showUserInfoPage(@RequestParam Long page, @RequestParam Long pagesize) {
        PageResult<UserInfoVo> pageResult = userService.findPage(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 用户详情
     *
     * @param userId
     * @return
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity findUserDetail(@PathVariable(value = "userId") long userId) {
        UserInfo userInfo = userService.findById(userId);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 获取当前用户的所有视频分页列表
     */
    @GetMapping("/videos")
    public ResponseEntity userVideoList(@RequestParam Long page, @RequestParam Long pagesize, @RequestParam Long uid) {
        PageResult<Video> pageResult = userService.findVideos(page, pagesize, uid);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 获取当前用户的所有动态分页列表
     */
    @GetMapping("/messages")
    public ResponseEntity findAllMovements(@RequestParam(defaultValue = "1") Long page,
                                           @RequestParam(defaultValue = "10") Long pagesize,
                                           @RequestParam(required = false) Long uid,
                                           @RequestParam(required = false) String state) {
        PageResult<MomentDateStrVo> pageResult = userService.findAllMovements(page, pagesize, uid, 0);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 根据id查询，动态详情
     */
    @PostMapping("/messages/{publishId}")
    public ResponseEntity findMovementById(@PathVariable("publishId") String publishId) {
        Moment moment = userService.findMovementById(publishId);
        return ResponseEntity.ok(moment);
    }

    /**
     * 查看动态的评论列表
     */
    @GetMapping("/messages/comments")
    public ResponseEntity findAllComments(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long pagesize,
            String messageID) {
        PageResult pageResult = userService.findAllComments(page, pagesize, messageID);
        return ResponseEntity.ok(pageResult);
    }

}
