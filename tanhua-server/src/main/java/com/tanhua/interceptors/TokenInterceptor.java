package com.tanhua.interceptors;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.constant.RedisKeyConstant;
import com.tanhua.domain.db.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author user_Chubby
 * @date 2021/5/6
 * @Description
 */
@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("进入了TokenInterceptor拦截器............");
        //1. 记录用户访问的url地址
        log.info("访问: " + request.getRequestURI());
        //2. 判断用户是否登陆过, token是否存在
        String token = request.getHeader("Authorization");
        if(StringUtils.isNotEmpty(token)) {
            //3. 如果存在，获取登陆用户的信息，且存入Threadlocal里,返回true
            User loginUser = getUserByToken(token);
            if(null != loginUser){
                // 不为空，说明redis有值，在有效期内登陆过
                //存入Threadlocal里,返回true
                UserHolder.setUser(loginUser);
                return true;
            }
        }
        //4. 如果不存在，报401错误, 返回false
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }

    public User getUserByToken(String token){
        //   从token中获取用户信息，获取id
        //        从redis来获取用户信息,token只是作为key
        String tokenKey = RedisKeyConstant.TOKEN + token;
        // 用户信息
        String userString = (String)redisTemplate.opsForValue().get(tokenKey);
        //        用户信息是null, 说明token过期了，重新登陆
        if(StringUtils.isEmpty(userString)){
            //throw new TanhuaException("登陆超时，请重新登陆");
            return null;
        }
        User loginUser = JSON.parseObject(userString, User.class);
        // 延长token有效期
        redisTemplate.expire(tokenKey, 7, TimeUnit.DAYS);
        return loginUser;
    }
}
