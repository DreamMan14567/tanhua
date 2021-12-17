package com.tanhua.dubbo.api;

import com.tanhua.domain.db.User;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */
public interface UserApi {

    /**
     * 根据电话号码查找用户
     *
     * @param phone
     * @return
     */
    User findByMobile(String phone);

    /**
     * 保存用户并返回用户主键
     *
     * @param user
     * @return
     */
    Long save(User user);

    /**
     * 手机号更新
     */
    void updatePhone(User user);

}
