package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Question;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
public interface QuestionApi {
    /**
     * 查询陌生人问题
     *
     * @param userId
     * @return
     */
    Question queryQuestion(Long userId);

    /**
     * 用户通用---设置陌生人问题 更新
     * @param newQuestion
     */
    void updateQuestion(Question newQuestion);

    /**
     * 用户通用---设置陌生人问题 添加
     * @param newQuestion
     */
    void insertQuestion(Question newQuestion);
}
