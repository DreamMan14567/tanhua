package com.tanhua.dubbo.api.mongo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.*;
import com.tanhua.dubbo.api.TestSoulApi;
import com.tanhua.dubbo.mapper.*;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */

@Service
public class TestSoulApiImpl implements TestSoulApi {
    @Autowired
    private DimensionMapper dimensionMapper;

    @Autowired
    private ResultMapper resultMapper;

    @Autowired
    private ConclusionMapper conclusionMapper;

    @Autowired
    private TestPageMapper testPageMapper;

    @Autowired
    private QuestionsMapper questionsMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private OptionsMapper optionsMapper;

    /**
     * 查询结果集
     *
     * @param reportId
     * @return
     */
    @Override
    public Result getResult(Long reportId) {
        QueryWrapper<Result> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", reportId);
        return resultMapper.selectOne(queryWrapper);
    }

    /**
     * 获取维度值
     *
     * @param reportId
     * @return
     */
    @Override
    public List<Dimension> getDimensions(Long reportId) {
        return dimensionMapper.selectList1(reportId);
    }

    /**
     * 获取结论
     *
     * @param conclusionId
     * @return
     */
    @Override
    public Conclusion getConclusion(Long conclusionId) {
        QueryWrapper<Conclusion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", conclusionId);
        return conclusionMapper.selectOne(queryWrapper);
    }

    @Override
    public Result getResult(Long loginUserId, Long id) {
        QueryWrapper<Result> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUserId);
        queryWrapper.eq("page_id", id);
        return resultMapper.selectOne(queryWrapper);
    }

    @Override
    public List<Page> getAllTestPage() {
        QueryWrapper<Page> queryWrapper = new QueryWrapper<>();
        return testPageMapper.selectList(queryWrapper);
    }
    @Override
    public List<Questions> getQuestionsByPageId(Long id) {
        QueryWrapper<Questions> questionsQueryWrapper = new QueryWrapper<>();
        questionsQueryWrapper.eq("page_id", id);
        return questionsMapper.selectList(questionsQueryWrapper);
    }

    /**
     * @param loginUserId 登录用户ID
     * @param id          试卷ID
     * @return
     */
    @Override
    public Boolean isAnswered(Long loginUserId, Long id) {
        QueryWrapper<Questions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("page_id", id);
        List<Questions> questionsList = questionsMapper.selectList(queryWrapper);

        if (questionsList != null) {
            List<Long> questionIds = questionsList.stream().map(Questions::getId).collect(Collectors.toList());
            Long qId = questionIds.get(0);
            QueryWrapper<Answer> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("user_id", id);
            queryWrapper1.eq("question_id", qId);
            return null != answerMapper.selectOne(queryWrapper1);
        }
        return false;
    }

    @Override
    public List<QuestionOption> getQuestionOptions(Long questionId) {
        QueryWrapper<QuestionOption> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("question_id",questionId);
        return optionsMapper.selectList(queryWrapper);
    }

    @Override
    public int insertResult(Result result) {
        return resultMapper.insert(result);
    }
}
