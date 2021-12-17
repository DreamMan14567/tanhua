package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SettingsVo implements Serializable {
    private Long id;
    private String strangerQuestion;
    private String phone;
    private Boolean likeNotification;
    private Boolean pinglunNotification;
    private Boolean gonggaoNotification;
}