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
     * 登录第一步 手机号登录-发送验证码
     *
     * @param phone
     */
    public void sendValidateCode(String phone) {
        // 判断上一次验证码是否失效
        String key = RedisKeyConstant.LOGIN_VALIDATE_CODE + phone;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        // 判断上一次验证码是否仍然生效
        if (StringUtils.isNotEmpty(codeInRedis)) {
            // 如果没失效，则抛出异常
            throw new TanHuaException("上一次验证码未失效");
        }
        // 失效则发送验证码
//        String validateCode = RandomStringUtils.randomNumeric(4);
//        log.info("手机号为：{}，验证码为：{}", phone, validateCode);
        String validateCode = "123456";
//        Map<String, String> result = smsTemplate.sendValidateCode(phone, validateCode);
        // 判断发送结果是否为空
//        if (CollectionUtils.isEmpty(result)) {
//            throw new TanHuaException("发送验证码失败");
//        }
        // 把验证码存入redis中
        redisTemplate.opsForValue().set(key, validateCode, 5, TimeUnit.MINUTES);
    }


    /**
     * 登录第二步---用户登录验证
     *
     * @param phone
     * @param verificationCode
     * @return
     */
    public Map<String, Object> loginRegInfo(String phone, String verificationCode) {
        // 判断验证码是否已经失效
        String key = RedisKeyConstant.LOGIN_VALIDATE_CODE + phone;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(codeInRedis)) {
            throw new TanHuaException("验证码失效");
        }
        // 判断输入的验证码与redis中的验证码是否相同
        if (!StringUtils.equals(codeInRedis, verificationCode)) {
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        // 如果上述有一处问题，则抛出异常
        // 如果都没有问题,则进行用户是否新用户判断
        Boolean isNew = false;
        User user = userApi.findByMobile(phone);
        // 发送到broker中的msg
        Map<String,Object> msg = new HashMap<>();
        // 如果是新用户 则进行插入操作
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
        // 如果不是新的，啥也不用干，直接登录
        // 删除验证码 防止重复登录
        redisTemplate.delete(key);
        // 同时生成token令牌，存入到redis中
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
     * 新用户---1填写资料
     *
     * @param userInfoVo 用户数据信息
     * @return
     */
    public void loginRegInfo(UserInfoVo userInfoVo) {
        // 填充用户的ID
        Long loginUserId = UserHolder.getId();
        // 构建UserInfo对象进行添加
        UserInfo userInfo = new UserInfo();
        // 属性复制
        BeanUtils.copyProperties(userInfoVo, userInfo);
        userInfoVo.setId(loginUserId);
        // 调用远程API进行添加
        userInfoApi.fillUserInformation(userInfo);
    }

    /**
     * 新用户--2 选取头像
     *
     * @param headPhoto 头像
     */
    public void selectAvatar(MultipartFile headPhoto) {
        Long loginUserId = UserHolder.getId();
        if (null == headPhoto) {
            throw new TanHuaException("请选择头像");
        }
        try {
            boolean detectResult = faceRecTemplate.detect(headPhoto.getBytes());
            log.info("detectResult:{}" + detectResult);
            if (detectResult) {
                throw new TanHuaException(ErrorResult.faceError());
            }
            // 把头像添加到阿里云
            String filename = headPhoto.getOriginalFilename();
            InputStream is = headPhoto.getInputStream();
            // 获取保存的地址
            assert filename != null;
            String newAvatar = ossTemplate.upload(filename, is);
            // 获取用户详细信息
            UserInfo userInfo = new UserInfo();
            userInfo.setId(loginUserId);
            userInfo.setAvatar(newAvatar);
            // 对用户详细信息的地址进行更新
            userInfoApi.updateAvatar(userInfo);
        } catch (IOException e) {
            throw new TanHuaException("上传头像失败");
        }

    }


}
