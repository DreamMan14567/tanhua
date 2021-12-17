package com.tanhua.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.service.UserInfoService;
import com.tanhua.service.UserLikeService;
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
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 用户资料读取
     * @param userID
     * @param huanxinID
     * @return
     */
    @GetMapping
    public ResponseEntity readUserInfo(Long userID,Long huanxinID){
        UserInfoVo userInfoVo = userInfoService.readUserInfo();
        return ResponseEntity.ok(userInfoVo);
    }

    /**
     * 用户资料 - 保存更新
     * @return
     */
    @PutMapping
    public ResponseEntity saveInfo(@RequestBody UserInfoVo userInfoVo){
        userInfoService.save(userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 用户资料 - 保存头像
     */
    @PostMapping("/header")
    public ResponseEntity updateAvatar(MultipartFile headPhoto){
        userInfoService.updateAvatar(headPhoto);
        return ResponseEntity.ok(null);
    }


}
