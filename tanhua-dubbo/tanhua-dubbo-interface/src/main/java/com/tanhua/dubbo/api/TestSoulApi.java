package com.tanhua.dubbo.api;

import com.tanhua.domain.db.*;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */


public interface TestSoulApi {
    /**
     * 查询结果集
     *
     * @param reportId
     * @return
     */
    Result getResult(Long reportId);

    /**
     * 获取维度值
     *
     * @param dimensionId
     * @return
     */
    List<Dimension> getDimensions(Long dimensionId);

    /**
     * 获取结论
     *
     * @param conclusionId
     * @return
     */
    Conclusion getConclusion(Long conclusionId);

    Result getResult(Long loginUserId, Long id);

    List<Page> getAllTestPage();

    List<Questions> getQuestionsByPageId(Long id);

    Boolean isAnswered(Long loginUserId, Long id);

    List<QuestionOption> getQuestionOptions(Long questionId);

    int insertResult(Result result);

}
