package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.commons.exceptions.TanHuaException;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.interceptor.AdminHolder;
import com.tanhua.manage.mapper.AdminMapper;
import com.tanhua.manage.utils.JwtUtils;
import com.tanhua.manage.vo.AdminVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private static final String CACHE_KEY_CAP_PREFIX = "MANAGE_CAP_";
    public static final String CACHE_KEY_TOKEN_PREFIX = "MANAGE_TOKEN_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 保存生成的验证码
     *
     * @param uuid
     * @param code
     */
    public void saveCode(String uuid, String code) {
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        // 缓存验证码，10分钟后失效
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));
    }

    /**
     * 获取登陆用户信息
     *
     * @return
     */
    public Admin getByToken(String authorization) {
        String token = authorization.replaceFirst("Bearer ", "");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        String adminString = (String) redisTemplate.opsForValue().get(tokenKey);
        Admin admin = null;
        if (StringUtils.isNotEmpty(adminString)) {
            admin = JSON.parseObject(adminString, Admin.class);
            // 延长有效期 30分钟
            redisTemplate.expire(tokenKey, 30, TimeUnit.MINUTES);
        }
        return admin;
    }

    /**
     * 返回token
     *
     * @param param
     * @return
     */
    public Map<String, String> loginCheck(Map<String, String> param) {
        // 获取参数
        String username = param.get("username");
        String password = param.get("password");
        String verificationCode = param.get("verificationCode");
        String uuid = param.get("uuid");
        // 判断账号密码是否为空
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new TanHuaException("密码账号不能为空");
        }
        // 判断验证码是否正确
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        String validateCode = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.equalsIgnoreCase(verificationCode, validateCode)) {
            throw new TanHuaException("验证码不正确");
        }
        // 判断密码是否正确
        // 需要先把传过来的密码进行加密
        String encryptPwd = SecureUtil.md5(password);
        // 查询账号密码是否存在数据库中
        Admin admin = query().eq("username", username).eq("password", encryptPwd).one();
        // 如果存在，则登录，并签发token
        if (null == admin) {
            throw new TanHuaException("账号或者密码错误");
        }
        // token记录在redis中
        String token = jwtUtils.createJWT(admin.getUsername(), admin.getId());
        String adminJson = JSON.toJSONString(admin);
        redisTemplate.opsForValue().set(CACHE_KEY_TOKEN_PREFIX + token, adminJson, Duration.ofHours(1));
        // 返回token
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        return tokenMap;
    }

    public AdminVo queryInfo() {
        // 获取管理员ID
        Admin admin = AdminHolder.getAdmin();
        // 构建vo对象
        AdminVo vo = new AdminVo();
        BeanUtils.copyProperties(admin, vo);
        // 封装属性进行返回
        return vo;
    }

    public void logout(String token) {
        token = token.replace("Bearer ", "");
        String key = CACHE_KEY_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }
}
