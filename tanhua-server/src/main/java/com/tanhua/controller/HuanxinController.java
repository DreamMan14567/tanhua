package com.tanhua.controller;

import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.vo.HuanXinUser;
import com.tanhua.interceptors.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */
@RestController
@RequestMapping("/huanxin")
public class HuanxinController {


    @GetMapping("/user")
    public ResponseEntity link(){
        HuanXinUser user = new HuanXinUser(UserHolder.getId().toString(), "123456",String.format("今晚打老虎_%d",100));
        return ResponseEntity.ok(user);
    }
}
