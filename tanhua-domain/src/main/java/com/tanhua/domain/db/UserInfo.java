package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/4
 * @Description
 */
@Data
public class UserInfo extends BasePojo{
    @TableId(type=IdType.INPUT)
    private Long id;
    private String nickname;
    private String avatar;
    private String tags;
    private String gender;
    private String age;
    private String education;
    private String city;
    private String birthday;
    private String coverPic;
    private String profession;
    private String income;
}
