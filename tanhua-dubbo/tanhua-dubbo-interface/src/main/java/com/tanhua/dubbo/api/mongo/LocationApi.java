package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/18
 * @Description
 */
public interface LocationApi {
    /**
     * 上报地理位置
     *
     * @param latitude
     * @param longitude
     * @param addrStr
     * @param loginUserId
     */
    void report(Double latitude, Double longitude, String addrStr, Long loginUserId);

    void addLocation(Double latitude, Double longitude, String addrStr, Long loginUserId);

    List<UserLocationVo> searchNear(Long userId, Long miles);
}
