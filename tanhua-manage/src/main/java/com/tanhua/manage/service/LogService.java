package com.tanhua.manage.service;

import com.tanhua.manage.domain.Log;
import com.tanhua.manage.mapper.LoggerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author user_Chubby
 * @date 2021/5/20
 * @Description
 */

@Service
public class LogService {
    @Autowired
    private LoggerMapper loggerMapper;

    public void addLog(Log addLog) {
        loggerMapper.insert(addLog);
    }
}
