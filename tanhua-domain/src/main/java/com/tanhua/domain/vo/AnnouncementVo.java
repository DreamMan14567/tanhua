package com.tanhua.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author user_Chubby
 * @date 2021/5/8
 * @Description
 */
@Data
public class AnnouncementVo {
    private Long id;
    private String title;
    private String description;
    private String created;
}
