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
public class Result extends BasePojo {
    // 结果主键
    private Long id;
    // 试卷ID
    private Long pageId;
    // 结论ID
    private Long conclusionId;
//    // 用于指向维度ID
//    private Long dimensionId;
    // 用户ID
    private Long userId;
}
