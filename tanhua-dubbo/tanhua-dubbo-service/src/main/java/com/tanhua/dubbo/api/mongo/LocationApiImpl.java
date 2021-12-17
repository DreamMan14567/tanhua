package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/18
 * @Description
 */


@Service
public class LocationApiImpl implements LocationApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 上报地理位置
     *
     * @param latitude
     * @param longitude
     * @param addrStr
     * @param loginUserId
     */
    @Override
    public void report(Double latitude, Double longitude, String addrStr, Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        long millis = System.currentTimeMillis();
        if (mongoTemplate.exists(query, UserLocation.class)) {
            // 更新
            Update update = new Update();
            update.set("latitude", latitude);
            update.set("longitude", latitude);
            update.set("lastUpdated", System.currentTimeMillis());
            update.set("addStr", new GeoJsonPoint(latitude, longitude));
            mongoTemplate.updateFirst(query, update, UserLocation.class);
        } else {
            // 添加
            UserLocation location = new UserLocation();
            location.setAddress(addrStr);
            location.setId(ObjectId.get());
            location.setUserId(loginUserId);
            location.setCreated(millis);
            location.setLastUpdated(millis);
            location.setUpdated(millis);
            location.setLocation(new GeoJsonPoint(latitude, longitude));
            mongoTemplate.save(location);
        }
    }

    @Override
    public void addLocation(Double latitude, Double longitude, String addrStr, Long loginUserId) {
        UserLocation userLocation = new UserLocation();
        userLocation.setUserId(loginUserId);
        userLocation.setLocation(new GeoJsonPoint(latitude, longitude));
        userLocation.setLastUpdated(System.currentTimeMillis());
        userLocation.setAddress(addrStr);
        mongoTemplate.insert(userLocation);
    }

    /**
     * 搜附近
     * @param userId
     * @param miles
     * @return
     */
    @Override
    public List<UserLocationVo> searchNear(Long userId, Long miles) {
        //1、根据用户id，查询当前用户的位置
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);
        //2、指定查询的半径范围
        GeoJsonPoint location = userLocation.getLocation();
        Distance distance = new Distance(miles/1000, Metrics.KILOMETERS);
        //3、根据此半径画圆
        Circle circle = new Circle(location,distance); //圆点，半径
        //4、调用mongotemplate查询 List<UserLocation>
        Query nearQuery = new Query(
                Criteria.where("location").withinSphere(circle)
        );
        List<UserLocation> userLocations = mongoTemplate.find(nearQuery, UserLocation.class);
        //5、转化为List<UserLocationVo>
        return UserLocationVo.formatToList(userLocations);
    }
}
