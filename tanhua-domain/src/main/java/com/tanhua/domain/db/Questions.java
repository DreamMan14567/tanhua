package com.tanhua.domain.db;

import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */
@Data
public class Questions extends BasePojo {
    // 问题ID
    private Long id;
    // 问题选项
    private String name;
    // 问题类型
    private Integer typeId;
    //所属试卷ID
    private Long pageId;
}
