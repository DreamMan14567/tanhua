package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */
@Service
public class UserApiImpl implements UserApi {
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据电话号码查找用户
     *
     * @param phone
     * @return
     */
    @Override
    public User findByMobile(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", phone);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 保存用户并返回用户主键
     *
     * @param user
     * @return
     */
    @Override
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 手机号更新
     *
     * @param user
     */
    @Override
    public void updatePhone(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", user.getId());
        userMapper.update(user, queryWrapper);
    }
}
