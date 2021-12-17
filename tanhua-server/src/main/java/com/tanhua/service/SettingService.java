package com.tanhua.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.commons.constant.RedisKeyConstant;
import com.tanhua.commons.exceptions.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.*;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingApi;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */

@Slf4j
@Service
public class SettingService {
    @Reference
    private SettingApi settingApi;

    @Reference
    private UserApi userApi;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 查询用户设置
     *
     * @return
     */
    public SettingsVo querySetting() {
        // 通过token获取用户ID
        User user = UserHolder.getUser();
        Long userId = user.getId();
        // 查找用户所有设置
        Settings settings = settingApi.queryUserById(userId);
        // setting属性复制到vo
        SettingsVo vo = new SettingsVo();
        if (settings != null) {
            BeanUtils.copyProperties(settings, vo);
        }
        // 通过QuestionApi复制属性到vo
        Question question = questionApi.queryQuestion(userId);
        if (question != null) {
            vo.setStrangerQuestion(question.getTxt());
        }
        vo.setId(userId);
        vo.setPhone(user.getMobile());
        // 回传数据
        log.info("vo:{}", vo);
        return vo;
    }

    /**
     * 保存陌生人问题
     *
     * @param question
     */
    public void saveQuestion(String question) {
        Long loginUserId = UserHolder.getId();
        // 通过userId获取对应的问题
        Question originQuestion = questionApi.queryQuestion(loginUserId);
        // 构建新的question对象
        Question newQuestion = new Question();
        // 属性复制
        // 判断问题对象是否已经存在
        if (null != originQuestion) {
            // 存在则更新
            BeanUtils.copyProperties(originQuestion, newQuestion);
            newQuestion.setTxt(question);
            newQuestion.setUpdated(new Date());
            questionApi.updateQuestion(newQuestion);
        } else {
            // 不存在则添加
            newQuestion.setTxt(question);
            newQuestion.setUserId(loginUserId);
            newQuestion.setCreated(new Date());
            newQuestion.setUpdated(new Date());
            questionApi.insertQuestion(newQuestion);
        }
    }

    /**
     * 通知设置--保存
     *
     * @param vo
     */
    public void saveNotification(SettingsVo vo) {
        Long loginUserId = UserHolder.getId();
        // 获取setting对象
        Settings settings = settingApi.queryUserById(loginUserId);
        // 构建一个新的setting对象
        Settings newSettings = new Settings();
        // vo对象属性赋值到构建对象中
        BeanUtils.copyProperties(vo, newSettings);
        // 属性补充
        newSettings.setUserId(loginUserId);
        // 判断查到的setting对象是否为空
        if (null == settings) {
            // 如果为空 添加
            settingApi.insertSetting(newSettings);
        } else {
            // 如果不为空 更新
            settingApi.updateSetting(newSettings);
        }
    }

    /**
     * 分页显示黑名单
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<UserInfoAgeVo> queryBlackList(Long page, Long pagesize) {
        Long loginUserId = UserHolder.getId();
        // 获取黑名单列表
        PageResult pageResult = settingApi.findBlackListPage(loginUserId, page, pagesize);
        List<BlackList> blackLists = pageResult.getItems();

        if (CollectionUtils.isNotEmpty(blackLists)) {
            // 获取黑名单中所有用户的id
            List<Long> blackListIds = blackLists.stream().map(BlackList::getUserId).collect(Collectors.toList());
            // 通过userId批量获取用户详情
            List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(blackListIds);
            // 流式转换为UserInfoMap
            // 补充黑名单信息 信息设计userinfo表
            List<UserInfoAgeVo> voList = new ArrayList<>();
            userInfoList.stream().map(userInfo -> {
                UserInfoAgeVo vo = new UserInfoAgeVo();
                BeanUtils.copyProperties(userInfo, vo);
                vo.setAge(Integer.valueOf(userInfo.getAge()));
                voList.add(vo);
                return vo;
            }).collect(Collectors.toList());
            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 移除黑名单成员
     *
     * @param blackerId
     */
    public void deleteBlacker(Long blackerId) {
        Long loginUserId = UserHolder.getId();
        BlackList blackList = new BlackList();
        blackList.setBlackUserId(blackerId);
        blackList.setUserId(loginUserId);
        settingApi.deleteBlacker(blackList);
    }

    /**
     * 修改手机号- 1 发送短信验证码
     */
    public void sendValidateCode() {
        User loginUser = UserHolder.getUser();
        // 判断之前发送的验证码是否过时
        String validateKey = RedisKeyConstant.CHANGE_NUM_VALIDATE_CODE + loginUser.getMobile();
        // 未过时则发出提示
        if (null == redisTemplate.opsForValue().get(validateKey)) {
            throw new TanHuaException("上一次验证码未过期");
        }
        // 如果过时发送验证码
        // 发送验证码
        String validateCode = RandomStringUtils.randomNumeric(4);
        log.info("发送验证码未：{}" + validateCode);
        redisTemplate.opsForValue().set(validateKey, validateCode, 5, TimeUnit.MINUTES);
    }

    /**
     * 修改手机号--校验验证码
     *
     * @param verificationCode
     * @return
     */
    public Map<String, Boolean> checkVerification(String verificationCode) {
        Map<String, Boolean> res = new HashMap<>();
        // 获取之前的验证码
        String validateKey = RedisKeyConstant.CHANGE_NUM_VALIDATE_CODE;
        String validateCode = (String) redisTemplate.opsForValue().get(validateKey);
        // 判断是还有效
        if (StringUtils.isEmpty(validateCode)) {
            log.info("验证码失效");
            res.put("verification", false);
            return res;
//            throw new TanHuaException("验证码失效");
        }
        if (!StringUtils.equalsIgnoreCase(verificationCode, validateCode)) {
            log.info("验证码错误");
            res.put("verification", false);
            return res;
//            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        // 到这里说明验证成功
        redisTemplate.delete(validateKey);
        res.put("verification", true);
        return res;
    }

    /**
     * 修改手机号--保存
     *
     * @param phone
     */
    public void updatePhone(String phone, String token) {
        User user = UserHolder.getUser();
        // 对用户的手机号码进行更新
        User phone4User = userApi.findByMobile(phone);
        // 判断用户新添加的手机账号是否已经被注册
        if (null == phone4User) {
            // 如果被注册，则抛出异常
            throw new TanHuaException("手机用户已经占用");
        }
        // 如果不是错误的，则进行属性更新
        User newUser = new User();
        BeanUtils.copyProperties(user, newUser);
        newUser.setMobile(phone);
        newUser.setCreated(new Date());
        // 调用API进行更新修改
        userApi.updatePhone(newUser);

        // 修改密码之后，删除原来的token
        String tokenKey = RedisKeyConstant.TOKEN + token;
        redisTemplate.delete(tokenKey);
    }
}
