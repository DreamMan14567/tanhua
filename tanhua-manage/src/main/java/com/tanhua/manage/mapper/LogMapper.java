package com.tanhua.manage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogMapper {

    /**
     * 过去?天活跃用户数
     *
     * @param date
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time>#{date}")
    Long countActiveUserAfterDate(String date);

    /**
     * 统计今日的活跃用户
     * @param today
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time=#{date}")
    Long numActive(String today);

    /**
     * 统计今日登录次数
     *
     */
    @Select("select count(distinct user_id) from tb_log where type = 1011 and log_time = #{date}")
    Long numLogin(String date);

}
