package com.tanhua.domain.db;

import com.tanhua.domain.db.BasePojo;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */

@Data
public class Answer extends BasePojo {
    // 回答主键
    private Long id;
    // 问题ID
    private Long questionId;
    // 选项ID
    private Long optionId;
    // 回答用户ID
    private Long userId;

}
