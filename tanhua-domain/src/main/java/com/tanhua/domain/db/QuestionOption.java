package com.tanhua.domain.db;

import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/24
 * @Description
 */
@Data
public class QuestionOption extends BasePojo {
    private Long id;
    private String optionName;
    private Long questionId;
    private Long score;
}
