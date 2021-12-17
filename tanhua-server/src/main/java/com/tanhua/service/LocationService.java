package com.tanhua.service;

import com.mongodb.BasicDBObject;
import com.tanhua.commons.exceptions.TanHuaException;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.UserLocationVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.LocationApi;
import com.tanhua.interceptors.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/18
 * @Description
 */

@Service
public class LocationService {
    @Reference
    private LocationApi locationApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 上报地理位置
     *
     * @param latitude
     * @param longitude
     * @param addrStr
     */
    public void report(Double latitude, Double longitude, String addrStr) {
        Long loginUserId = UserHolder.getId();
        locationApi.report(latitude, longitude, addrStr, loginUserId);
    }

    /**
     * 搜附近
     *
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> searchNearBy(String gender, String distance) {

        System.out.println("======================");
        //1、获取当前用户id
        Long userId = UserHolder.getId();
        //2、调用API根据用户id，距离查询当前用户附近的人 List<UserLocationVo>
        List<UserLocationVo> locations = locationApi.searchNear(userId, Long.valueOf(distance));
        //3、循环附近的人所有数据
        List<NearUserVo> userVoList = new ArrayList<>();
        for (UserLocationVo location : locations) {
            //4、调用UserInfoApi查询用户数据，构造NearUserVo对象
            UserInfo info = userInfoApi.findUserInfoById(location.getUserId());
            if (info.getId().toString().equals(userId.toString())) {  //排除自己
                continue;
            }
            if (gender != null && !info.getGender().equals(gender)) { //排除性别不符合
                continue;
            }
            NearUserVo vo = new NearUserVo();
            vo.setUserId(info.getId());
            vo.setAvatar(info.getAvatar());
            vo.setNickname(info.getNickname());
            userVoList.add(vo);
        }
        return userVoList;
    }
}
