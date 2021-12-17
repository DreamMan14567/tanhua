package com.tanhua.domain.vo;

import com.tanhua.domain.db.QuestionOption;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/24
 * @Description
 */
@Data
public class QuestionsVo implements Serializable {
    // 问题ID
    private String id;

    private String question;

    private List<OptionVo> options;
}
