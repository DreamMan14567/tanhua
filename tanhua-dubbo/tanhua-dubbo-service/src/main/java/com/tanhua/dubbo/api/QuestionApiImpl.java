package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
@Service
public class QuestionApiImpl implements QuestionApi {
    @Autowired
    private QuestionMapper questionMapper;
    /**
     * 查询陌生人问题
     *
     * @param userId
     * @return
     */
    @Override
    public Question queryQuestion(Long userId) {
        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        questionQueryWrapper.eq("user_id",userId);
        return questionMapper.selectOne(questionQueryWrapper);
    }

    /**
     * 用户通用---设置陌生人问题 更新
     *
     * @param newQuestion
     */
    @Override
    public void updateQuestion(Question newQuestion) {
        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        questionQueryWrapper.eq("user_id",newQuestion.getUserId());
        questionMapper.update(newQuestion,questionQueryWrapper);
    }

    /**
     * 用户通用---设置陌生人问题 添加
     *
     * @param newQuestion
     */
    @Override
    public void insertQuestion(Question newQuestion) {
        questionMapper.insert(newQuestion);
    }
}
