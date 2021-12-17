package com.tanhua.domain.vo;

import com.tanhua.domain.db.Questions;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/24
 * @Description
 */

@Data
public class TestPageVo implements Serializable {
    // 试卷编号
    private String id;
    // 试卷名字
    private String name;
    // 等级表  初中高
    private String level;
    // 星级
    private Integer star;
    // 封面
    private String cover;
    // 问题列表
    private List<QuestionsVo> questions;
    // 是否解锁
    private Integer isLock;
    // 报告ID
    private String reportId;
}
