package com.tanhua.controller;

import com.tanhua.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */

@RestController
@RequestMapping("/user")
public class LoginController {
    @Autowired
    private UserService userService;

    /**
     * 登录验证第一步 发送验证码
     * @param param
     * @return
     */
    @RequestMapping("/login")
    public ResponseEntity sendValidateCode(@RequestBody Map<String,String> param){
        String phone = param.get("phone");
        userService.sendValidateCode(phone);
        return ResponseEntity.ok(null);
    }

    /**
     * 登录第二步---用户登录验证
     * @param validateMap 登录验证信息
     * @return
     */
    @PostMapping("/loginVerification")
    public ResponseEntity loginRegInfo(@RequestBody Map<String,String> validateMap){
        String phone = validateMap.get("phone");
        String verificationCode = validateMap.get("verificationCode");
        Map<String,Object> result = userService.loginRegInfo(phone,verificationCode);
        return ResponseEntity.ok(result);
    }
}
