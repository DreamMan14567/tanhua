package com.tanhua.manage.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import com.tanhua.manage.vo.DataPointVo;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/18
 * @Description
 */

@Service
public class AnalysisService {
    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;

    @Autowired
    private LogMapper logMapper;

    /**
     * 获取返回数据对象
     *
     * @return
     */
    public AnalysisSummaryVo getSummary() {
        // 今天
        String today = DateUtil.today();
        // 昨天
        String yesterday = DateUtil.yesterday().toDateStr();
        // 过去七天
        String last7days = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, -7).toDateStr();
        // 过去三十天
        String last30days = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, -30).toDateStr();

        // 统计总用户数
        Long cumulativeUsers = analysisByDayMapper.countUser();
        // 统计三十天活跃用户
        Long activePassMonth = logMapper.countActiveUserAfterDate(last30days);
        //统计过去七天活跃用户
        Long activePassWeek = logMapper.countActiveUserAfterDate(last7days);

        // 统计今日新增用户
        Long newUsersToday = analysisByDayMapper.numRegistered(today);
        Long newUsersYesterday = analysisByDayMapper.numRegistered(yesterday);
        // 今日新增用户涨跌率
        BigDecimal newUsersTodayRate = ComputeUtil.computeRate(newUsersToday, newUsersYesterday);
        // 今日登录次数
        QueryWrapper<AnalysisByDay> queryToday = new QueryWrapper<>();
        queryToday.eq("record_date", today);
        Long loginTimesToday = analysisByDayMapper.selectOne(queryToday).getNumLogin();
        QueryWrapper<AnalysisByDay> queryYesterday = new QueryWrapper<>();
        queryYesterday.eq("record_date", yesterday);
        Long loginTimesYesterday = analysisByDayMapper.selectOne(queryYesterday).getNumLogin();
        // 今日登录次数涨跌率
        BigDecimal loginTimesTodayRate = ComputeUtil.computeRate(loginTimesToday, loginTimesYesterday);
        // 今日活跃用户
        Long activeUsersToday = logMapper.countActiveUserAfterDate(today);
        // 昨日活跃用户
        Long activeUserYesterday = logMapper.countActiveUserAfterDate(yesterday);
        BigDecimal activeUsersTodayRate = ComputeUtil.computeRate(activeUsersToday, activeUserYesterday);

        AnalysisSummaryVo vo = new AnalysisSummaryVo();
        vo.setActiveUsersTodayRate(activeUsersTodayRate);
        vo.setLoginTimesTodayRate(loginTimesTodayRate);
        vo.setLoginTimesToday(loginTimesToday);
        vo.setNewUsersTodayRate(newUsersTodayRate);
        vo.setNewUsersToday(newUsersToday);
        vo.setActiveUsersToday(activeUsersToday);
        vo.setActivePassWeek(activePassWeek);
        vo.setActivePassMonth(activePassMonth);
        vo.setCumulativeUsers(cumulativeUsers);
        System.out.println(vo);
        return vo;
    }

    /**
     * 统计不同类型的数据
     *
     * @param startDate
     * @param endDate
     * @param type
     * @return
     */
    public AnalysisUsersVo getUsersCount(Long startDate, Long endDate, Integer type) {
        // 通过数字判断是那一项数据
        String column = "";

        switch (type){
            case 101: column="num_registered";break;
            case 102: column="num_active";break;
            case 103: column="num_retention1d";break;
            default:throw new BusinessException("参数格式不正确");
        }

        // 今年的开始日期
        String thisYearStart = DateUtil.date(startDate).toDateStr();
        String thisYearEnd = DateUtil.date(endDate).toDateStr();
        List<DataPointVo> thisYearData = analysisByDayMapper.findBetweenDate(thisYearStart,thisYearEnd,column);

        String lastYearStart = DateUtil.date(startDate).toDateStr();
        String lastYearEnd = DateUtil.date(endDate).toDateStr();
        List<DataPointVo> lastYearData = analysisByDayMapper.findBetweenDate(lastYearStart,lastYearEnd,column);

        AnalysisUsersVo vo = new AnalysisUsersVo();
        vo.setThisYear(thisYearData);
        vo.setLastYear(lastYearData);

        return vo;
    }


    public void getEveryDay(){
        //String yesterday = DateUtil.yesterday().toDateStr();
        String today = DateUtil.today();

        Long numRegistered = analysisByDayMapper.numRegistered(today);
        Long numLogin = analysisByDayMapper.numLogin(today);
        Long numActive = logMapper.numActive(today);

        AnalysisByDay analysisByDay = new AnalysisByDay();
        analysisByDay.setNumRegistered(numRegistered);
        analysisByDay.setNumLogin(numLogin);
        analysisByDay.setNumActive(numActive);

        //判断是否存在今天记录

        analysisByDayMapper.updateById(analysisByDay);
    }
}
