package com.tanhua.interceptors;

import com.tanhua.domain.db.User;
import org.springframework.stereotype.Component;

/**
 * @author user_Chubby
 * @date 2021/5/6
 * @Description
 */

public class UserHolder {
    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static void setUser(User user){
        userThreadLocal.set(user);
    }

    public static User getUser(){
        return userThreadLocal.get();
    }

    public static Long getId(){
        return userThreadLocal.get().getId();
    }
}
