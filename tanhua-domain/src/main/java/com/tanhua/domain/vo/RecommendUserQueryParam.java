package com.tanhua.domain.vo;

import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/9
 * @Description
 */

@Data
public class RecommendUserQueryParam {
    private long page;
    private long pagesize;
    private String gender;
    private String lastLogin;
    private String age;
    private String city;
    private String education;
}
