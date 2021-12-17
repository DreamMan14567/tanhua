package com.tanhua.controller;

import com.tanhua.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;
import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/18
 * @Description
 */


@RestController
@RequestMapping("/baidu")
public class LocationController {
    @Autowired
    private LocationService locationService;


    @RequestMapping("/location")
    public ResponseEntity reportLocation(@RequestBody Map<String,Object> param){
        Double latitude = (Double) param.get("latitude");
        Double longitude = (Double) param.get("longitude");
        String addrStr = (String) param.get("addrStr");

        locationService.report(latitude,longitude,addrStr);
        return ResponseEntity.ok(null);
    }

}
