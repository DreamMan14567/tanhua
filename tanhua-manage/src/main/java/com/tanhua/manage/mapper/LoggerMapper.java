package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.Log;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author user_Chubby
 * @date 2021/5/20
 * @Description
 */
@Mapper
public interface LoggerMapper extends BaseMapper<Log> {
}
