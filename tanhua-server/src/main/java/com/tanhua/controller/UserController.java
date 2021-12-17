package com.tanhua.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.service.UserLikeService;
import com.tanhua.service.UserService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 新用户---1 填写资料
     *
     * @param userInfoVo 用户数据信息
     * @return
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity loginReginfo(@RequestBody UserInfoVo userInfoVo) {
        userService.loginRegInfo(userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 新用户--2 选取头像
     * @param headPhoto
     * @return
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity selectAvatar(MultipartFile headPhoto){
        userService.selectAvatar(headPhoto);
        return ResponseEntity.ok(null);
    }




}
