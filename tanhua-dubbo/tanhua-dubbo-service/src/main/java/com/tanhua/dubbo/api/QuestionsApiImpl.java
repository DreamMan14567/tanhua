package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.QuestionOption;
import com.tanhua.domain.db.Questions;
import com.tanhua.dubbo.mapper.QuestionMapper;
import com.tanhua.dubbo.mapper.QuestionOptionMapper;
import com.tanhua.dubbo.mapper.QuestionsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.Query;
import java.util.List;
import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/24
 * @Description
 */
@Service
public class QuestionsApiImpl implements QuestionsApi {
    @Autowired
    private QuestionsMapper questionsMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Override
    public Questions queryQuestionsById(Integer id) {
        return questionsMapper.selectById(id);
    }

    @Override
    public Long getScore(Long id, Long optionId) {
        QueryWrapper<QuestionOption> wrapper = new QueryWrapper<>();
        wrapper.eq("question_id",id);
        wrapper.eq("id",optionId);
        return questionOptionMapper.selectOne(wrapper).getScore();
    }
}
