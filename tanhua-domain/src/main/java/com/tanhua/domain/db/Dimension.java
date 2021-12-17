package com.tanhua.domain.db;

import com.tanhua.domain.db.BasePojo;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */
@Data
public class Dimension extends BasePojo {
    // 维度主键ID
    private Long id;
    // 问题类型ID
    private Long typeId;//0 外箱 1 判断 2 抽象 3 理性
    // 维度名称
    private String key;
    // 维度值
    private String value;
    // result ID
    private Long reportId;

    private Long userId;
}
