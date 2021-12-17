package com.tanhua.controller;

import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.vo.*;
import com.tanhua.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
@RestController
@RequestMapping("/users")
public class SettingController {
    @Autowired
    private SettingService settingService;

    /**
     * 获取用户设置
     *
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity getSettings() {
        SettingsVo settingsVo = settingService.querySetting();
        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 设置陌生人问题--保存
     *
     * @param question
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity setStrangerQuestion(String question) {
        settingService.saveQuestion(question);
        return ResponseEntity.ok(null);
    }

    /**
     * 通知设置-- 保存
     *
     * @param vo
     * @return
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity saveNotification(@RequestBody SettingsVo vo) {
        settingService.saveNotification(vo);
        return ResponseEntity.ok(null);
    }

    /**
     * 分页显示黑名单
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/blacklist")
    public ResponseEntity blackList(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<UserInfoAgeVo> pageResult = settingService.queryBlackList(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 移除黑名单成员
     *
     * @param blackerId
     * @return
     */
    @DeleteMapping("/blacklist/{blackerId}")
    public ResponseEntity removeBlack(@PathVariable("blackUserId") Long blackerId) {
        settingService.deleteBlacker(blackerId);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity sendVerificationCode() {
        settingService.sendValidateCode();
        return ResponseEntity.ok(null);
    }

    @PostMapping("/phone/checkVerificationCode")
    public ResponseEntity checkVerification(@RequestBody Map<String, String> param) {
        String verificationCode = param.get("verificationCode");
        Map<String, Boolean> result = settingService.checkVerification(verificationCode);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/phone")
    public ResponseEntity updatePhone(@RequestBody Map<String, String> param, @RequestHeader("Authorization") String token) {
        String phone = param.get("phone");
        settingService.updatePhone(phone,token);
        return ResponseEntity.ok(null);
    }
}
