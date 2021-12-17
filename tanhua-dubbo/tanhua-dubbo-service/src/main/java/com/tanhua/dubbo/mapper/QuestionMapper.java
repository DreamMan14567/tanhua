package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Questions;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/6
 * @Description
 */
public interface QuestionMapper extends BaseMapper<Question> {

}
