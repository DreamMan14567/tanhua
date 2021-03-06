package com.tanhua.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.commons.constant.RedisKeyConstant;
import com.tanhua.commons.exceptions.TanHuaException;
import com.tanhua.commons.templates.FaceRecTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */

@Service
@Slf4j
public class UserService {
    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private UserApi userApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FaceRecTemplate faceRecTemplate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * ??????????????? ???????????????-???????????????
     *
     * @param phone
     */
    public void sendValidateCode(String phone) {
        // ????????????????????????????????????
        String key = RedisKeyConstant.LOGIN_VALIDATE_CODE + phone;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        // ??????????????????????????????????????????
        if (StringUtils.isNotEmpty(codeInRedis)) {
            // ?????????????????????????????????
            throw new TanHuaException("???????????????????????????");
        }
        // ????????????????????????
//        String validateCode = RandomStringUtils.randomNumeric(4);
//        log.info("???????????????{}??????????????????{}", phone, validateCode);
        String validateCode = "123456";
//        Map<String, String> result = smsTemplate.sendValidateCode(phone, validateCode);
        // ??????????????????????????????
//        if (CollectionUtils.isEmpty(result)) {
//            throw new TanHuaException("?????????????????????");
//        }
        // ??????????????????redis???
        redisTemplate.opsForValue().set(key, validateCode, 5, TimeUnit.MINUTES);
    }


    /**
     * ???????????????---??????????????????
     *
     * @param phone
     * @param verificationCode
     * @return
     */
    public Map<String, Object> loginRegInfo(String phone, String verificationCode) {
        // ?????????????????????????????????
        String key = RedisKeyConstant.LOGIN_VALIDATE_CODE + phone;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(codeInRedis)) {
            throw new TanHuaException("???????????????");
        }
        // ???????????????????????????redis???????????????????????????
        if (!StringUtils.equals(codeInRedis, verificationCode)) {
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        // ?????????????????????????????????????????????
        // ?????????????????????,????????????????????????????????????
        Boolean isNew = false;
        User user = userApi.findByMobile(phone);
        // ?????????broker??????msg
        Map<String,Object> msg = new HashMap<>();
        // ?????????????????? ?????????????????????
        if (null == user) {
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex(phone.substring(5)));
            Long userId = userApi.save(user);
            isNew = true;
            user.setId(userId);
            huanXinTemplate.register(userId);


            msg.put("userId",userId);
            msg.put("logTime", DateFormatUtils.format(new Date(),"yyyy-MM-dd"));
            msg.put("type","1012");
            rocketMQTemplate.convertAndSend("tanhua-log",msg);
        }
        // ???????????????????????????????????????????????????
        // ??????????????? ??????????????????
        redisTemplate.delete(key);
        // ????????????token??????????????????redis???
        String token = jwtUtils.createJWT(phone, user.getId());
        String userJSON = JSON.toJSONString(user);
        redisTemplate.opsForValue().set(RedisKeyConstant.TOKEN + token, userJSON, 7, TimeUnit.DAYS);
        Map<String, Object> result = new HashMap<>();
        result.put("isNew", isNew);
        result.put("token", token);

        msg.put("userId",user.getId());
        msg.put("logTime",DateFormatUtils.format(new Date(),"yyyy-MM-dd"));
        msg.put("type","1011");
//        rocketMQTemplate.convertAndSend("tanhua-log",msg);
        return result;
    }


    /**
     * ?????????---1????????????
     *
     * @param userInfoVo ??????????????????
     * @return
     */
    public void loginRegInfo(UserInfoVo userInfoVo) {
        // ???????????????ID
        Long loginUserId = UserHolder.getId();
        // ??????UserInfo??????????????????
        UserInfo userInfo = new UserInfo();
        // ????????????
        BeanUtils.copyProperties(userInfoVo, userInfo);
        userInfoVo.setId(loginUserId);
        // ????????????API????????????
        userInfoApi.fillUserInformation(userInfo);
    }

    /**
     * ?????????--2 ????????????
     *
     * @param headPhoto ??????
     */
    public void selectAvatar(MultipartFile headPhoto) {
        Long loginUserId = UserHolder.getId();
        if (null == headPhoto) {
            throw new TanHuaException("???????????????");
        }
        try {
            boolean detectResult = faceRecTemplate.detect(headPhoto.getBytes());
            log.info("detectResult:{}" + detectResult);
            if (detectResult) {
                throw new TanHuaException(ErrorResult.faceError());
            }
            // ???????????????????????????
            String filename = headPhoto.getOriginalFilename();
            InputStream is = headPhoto.getInputStream();
            // ?????????????????????
            assert filename != null;
            String newAvatar = ossTemplate.upload(filename, is);
            // ????????????????????????
            UserInfo userInfo = new UserInfo();
            userInfo.setId(loginUserId);
            userInfo.setAvatar(newAvatar);
            // ??????????????????????????????????????????
            userInfoApi.updateAvatar(userInfo);
        } catch (IOException e) {
            throw new TanHuaException("??????????????????");
        }

    }


}
