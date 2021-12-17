package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Questions;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/24
 * @Description
 */

public interface QuestionsApi {
    Questions queryQuestionsById(Integer id);

    Long getScore(Long id,Long optionId);
}
