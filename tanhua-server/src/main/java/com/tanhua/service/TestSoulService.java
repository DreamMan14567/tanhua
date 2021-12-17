package com.tanhua.service;

import com.tanhua.domain.db.*;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.FinalConclusionVo;
import com.tanhua.domain.vo.OptionVo;
import com.tanhua.domain.vo.QuestionsVo;
import com.tanhua.domain.vo.TestPageVo;
import com.tanhua.dubbo.api.ConclusionApi;
import com.tanhua.dubbo.api.DimensionAPi;
import com.tanhua.dubbo.api.QuestionsApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.dubbo.api.TestSoulApi;
import com.tanhua.interceptors.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */

@Service
@Slf4j
public class TestSoulService {
    @Reference
    private TestSoulApi testSoulApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private ConclusionApi conclusionApi;

    @Reference
    private QuestionsApi questionsApi;

    @Reference
    private DimensionAPi dimensionAPi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 通过reportId 查询结果，并封装为一个vo对象
     *
     * @param reportId
     * @return
     */
    public FinalConclusionVo getReport(Long reportId) {
        Long loginUserId = UserHolder.getId();
        // 根据ReportId 可以获取一个结果集对象
        Result result = testSoulApi.getResult(reportId);
        System.out.println(result);
        // 通过结果集对象的dimensionId获取维度结果 --> List<Dimension>
        log.info("reportId:{}", reportId);
        List<Dimension> dimensions = testSoulApi.getDimensions(reportId);
        log.info("dimensions:{}", dimensions);
        // 通过conclusionId 获取结论 --> Conclusion [cover，conclusion]
        Conclusion conclusion = testSoulApi.getConclusion(result.getConclusionId());
        // userID获取推荐人信息名单
        List<RecommendUser> recommendUserList = recommendUserApi.queryRecommendUserList(loginUserId);
        List<Long> ids = recommendUserList.stream().map(RecommendUser::getUserId).collect(Collectors.toList());
        List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(ids);
        // 封装为finalConclusionVo 返回
        FinalConclusionVo vo = new FinalConclusionVo();

        vo.setConclusion(conclusion.getConclusion());
        vo.setCover(conclusion.getCover());
        vo.setDimensions(dimensions);
        vo.setSimilarYou(userInfoList);
        return vo;
    }

    public List<TestPageVo> getTestPage() {
        Integer i = 0;

        Long loginUserId = UserHolder.getId();
        // 查询试卷
        List<Page> pageList = testSoulApi.getAllTestPage();
        // 先把试卷初级解锁
        redisTemplate.opsForValue().set("page" + i, "1");
        // 根据试卷ID查询对应的试题
        // 根据对应的试题ID查询所有的选项，添加到question中
        List<TestPageVo> voList = new ArrayList<>();
        for (Page page : pageList) {
            TestPageVo vo = new TestPageVo();
            List<Questions> questionsList = testSoulApi.getQuestionsByPageId(page.getId());
            // 进行问题选项复制
            List<QuestionsVo> questionsVoList = new ArrayList<>();
            for (Questions questions : questionsList) {
                QuestionsVo questionsVo = new QuestionsVo();
                // 获取问题所有的选项
                List<QuestionOption> questionOptions = testSoulApi.getQuestionOptions(questions.getId());
                List<OptionVo> optionVos = new ArrayList<>();
                for (QuestionOption questionOption : questionOptions) {
                    OptionVo vo1 = new OptionVo();
                    vo1.setOption(questionOption.getOptionName());
                    vo1.setId(questionOption.getId().toString());
                    optionVos.add(vo1);
                }
                //设置问题选项
                questionsVo.setQuestion(questions.getName());
                questionsVo.setOptions(optionVos);
                questionsVo.setId(questions.getId().toString());
                questionsVoList.add(questionsVo);
                log.info("questionVo:{}", questionsVo);
            }
            BeanUtils.copyProperties(page, vo);
            vo.setQuestions(questionsVoList);
            vo.setId(page.getId().toString());
            vo.setStar(page.getStar().intValue());

            vo.setIsLock(1);
            // 如何进行解锁和锁住 reids中存一个数，进行判断，如果>2，则全部解锁，如果>1则解锁前两份
            if (redisTemplate.opsForValue().get("page" + i) != null) {
                vo.setIsLock(0);
//                redisTemplate.delete("page" + i);
            }
            // 判断这套卷子是否已经被该用户回答过
            Boolean isAnswered = testSoulApi.isAnswered(loginUserId, page.getId());// true
            // 如果回答过了，则解锁下一套卷子
            if (isAnswered && (i + 1) < pageList.size()) {
                redisTemplate.opsForValue().set("page" + (i + 1), "1");
            }
            i = i + 1;//1
            // 获取报告ID
            Result result = testSoulApi.getResult(loginUserId, page.getId());
            if (null != result) {
                vo.setReportId(String.valueOf(result.getId()));
            }
            voList.add(vo);
        }
        // 封装即可
        return voList;
    }

    /**
     * 提交问卷
     * <p>
     * 根据问题id集合获取所有的questions ，进而获得各个类型的问题ids 从而算出各个类型的得分和总分 进而算出性格占比
     *
     * @param map 答题卡
     * @return
     */
    public String submit(Map<String, List<Answer>> map) {
        List<Answer> answerList = map.get("answers");
        Map<Long, Long> answers = answerList.stream().collect(Collectors.toMap(Answer::getQuestionId, Answer::getOptionId));

        /*
        插入结果表：
            添加试卷ID
            添加结论ID
        添加维度表：
            计算各个纬度值插入

         */
        List<Long> questionIds = answerList.stream().map(Answer::getQuestionId).collect(Collectors.toList());

        List<Questions> questionsList = questionIds.stream().map(id -> {
            Questions questions = questionsApi.queryQuestionsById(id.intValue());
            return questions;
        }).collect(Collectors.toList());

        Long pageId = questionsList.get(0).getPageId();

        List<Long> idsType0 = questionsList.stream().filter(questions -> questions.getTypeId() == 0).map(Questions::getId).collect(Collectors.toList());
        List<Long> idsType1 = questionsList.stream().filter(questions -> questions.getTypeId() == 1).map(Questions::getId).collect(Collectors.toList());
        List<Long> idsType2 = questionsList.stream().filter(questions -> questions.getTypeId() == 2).map(Questions::getId).collect(Collectors.toList());
        List<Long> idsType3 = questionsList.stream().filter(questions -> questions.getTypeId() == 3).map(Questions::getId).collect(Collectors.toList());
        // 算出各个维度总分
        Long[] dimensions = new Long[4];
        String[] dimensionsType = new String[]{"外向", "判断", "抽象", "理性"};
        Long score1 = getSum(idsType0, answers);
        dimensions[0] = score1 / idsType0.size() * 8L;
        Long score2 = getSum(idsType1, answers);
        dimensions[1] = score1 / idsType0.size() * 8L;
        Long score3 = getSum(idsType2, answers);
        dimensions[2] = score1 / idsType0.size() * 8L;
        Long score4 = getSum(idsType3, answers);
        dimensions[3] = score1 / idsType0.size() * 8L;
        // 我的总分
        Long youTotal = score1 + score2 + score3 + score4;
        Conclusion conclusion = yourConclusion(youTotal);
        //插入结果表之后获取返回ID


        Result result = new Result();
        result.setConclusionId(conclusion.getId());
        result.setUserId(UserHolder.getId());
        result.setPageId(pageId);
        Long reportId = (long) testSoulApi.insertResult(result);
        System.out.println("reportId:" + reportId);
        // 然后在添加维度表
        for (int i = 0; i < dimensions.length; i++) {
            Dimension dimension = new Dimension();
            dimension.setKey(dimensionsType[i]);
            dimension.setTypeId((long) i);
            dimension.setReportId(reportId);
            dimension.setValue(dimensions[i].toString());
            dimension.setUserId(UserHolder.getId());
            dimensionAPi.insertDimension(dimension);
        }
        return reportId.toString();
    }

    public Long getSum(List<Long> ids, Map<Long, Long> answer) {
        Long sum = 0L;
        for (Long id : ids) {
            sum += questionsApi.getScore(id, answer.get(id));
        }
        return sum;
    }

    public Conclusion yourConclusion(Long youTotal) {
        Conclusion conclusion;
        if (youTotal < 21) {
            conclusion = conclusionApi.getConclusion(1);
        } else if (youTotal < 60) {
            conclusion = conclusionApi.getConclusion(2);
        } else if (youTotal < 80) {
            conclusion = conclusionApi.getConclusion(3);
        } else {
            conclusion = conclusionApi.getConclusion(4);
        }
        return conclusion;
    }

}
