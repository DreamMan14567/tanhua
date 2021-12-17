package com.tanhua.controller;

import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.service.IMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */

@RestController
@RequestMapping("/messages")
public class IMController {
    @Autowired
    private IMService iMService;

    /**
     * 获取公告列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/announcements")
    public ResponseEntity announcementList(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult pageResult = iMService.getAnnouncementList(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 添加联系人
     *
     * @param param
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity addContract(@RequestBody Map<String, Long> param) {
        Long userId = param.get("userId");
        iMService.addLinkman(userId);
        return ResponseEntity.ok(null);
    }

    /**
     * 联系人列表
     */
    @GetMapping("/contacts")
    public ResponseEntity getLinkmenList(@RequestParam(defaultValue = "1") Long page
            , @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<ContactVo> pageResult = iMService.findPage(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 点赞评论喜欢
     */

    @GetMapping("/likes}")
    public ResponseEntity queryLikes(@RequestParam(defaultValue = "1") Long page
            , @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<MessageVo> pageResult = iMService.getMessageList(1, page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/loves}")
    public ResponseEntity queryLoves(@RequestParam(defaultValue = "1") Long page
            , @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<MessageVo> pageResult = iMService.getMessageList(3, page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/comments}")
    public ResponseEntity queryComments(@RequestParam(defaultValue = "1") Long page
            , @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<MessageVo> pageResult = iMService.getMessageList(2, page, pagesize);
        return ResponseEntity.ok(pageResult);
    }


}
