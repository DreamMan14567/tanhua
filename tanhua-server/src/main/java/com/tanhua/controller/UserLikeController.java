package com.tanhua.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.service.UserLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */

@RestController
@RequestMapping("/users")
public class UserLikeController {

    @Autowired
    private UserLikeService userLikeService;

    /**
     * 统计互相喜欢 喜欢 粉丝
     */
    @GetMapping("/counts")
    public ResponseEntity count() {
        CountsVo vo = userLikeService.count();
        return ResponseEntity.ok(vo);
    }

    @GetMapping("/friends/{type}")
    public ResponseEntity findPage(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize, @PathVariable int type) {
        PageResult<FriendVo> pageResult = userLikeService.findByPage4Type(page, pagesize, type);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/friends/fans/{userId}")
    public ResponseEntity fansLike(@PathVariable Long userId){
        userLikeService.fansLike(userId);
        return ResponseEntity.ok(null);
    }


}
