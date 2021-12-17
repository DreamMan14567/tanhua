package com.tanhua.domain.db;

import lombok.Data;


/**
 * 试卷
 */
@Data
public class Page extends BasePojo {
    // 试卷编号
    private Long id;
    // 试卷名字
    private String name;
    // 等级表  初中高
    private String level;
    // 星级
    private Long star;
    // 封面
    private String cover;
}
