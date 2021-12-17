package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/20
 * @Description
 */

@Component
@Slf4j
@RocketMQMessageListener(topic = "tanhua-log",consumerGroup = "loginGroup")
public class LogMessageListener implements RocketMQListener<String> {
    @Autowired
    private LogService logService;


    @Override
    public void onMessage(String message) {
        Map msg = JSON.parseObject(message,Map.class);
        Integer userId = (Integer) msg.get("userId");
        String type = (String) msg.get("type");
        String logTime = (String) msg.get("logTime");

        Log addLog = new Log();
        addLog.setUserId(userId.longValue());
        addLog.setType(type);
        addLog.setLogTime(logTime);

        logService.addLog(addLog);
    }
}
