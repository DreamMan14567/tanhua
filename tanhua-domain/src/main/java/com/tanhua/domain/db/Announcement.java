package com.tanhua.domain.db;

import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/8
 * @Description
 */
@Data
public class Announcement extends BasePojo {
    private Long id;
    private String title;
    private String description;
}
