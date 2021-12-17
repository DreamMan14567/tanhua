package com.tanhua.manage.domain;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/19
 * @Description
 */
@Data
public class MomentDateStrVo implements Serializable {
    private ObjectId id; //主键id
    private Long pid; //Long类型，用于推荐系统的模型
    private Long userId;
    private String textContent; //文字
    private Integer state;// 审核状态,0:未审核，1：审核通过，2：不通过

    private List<String> medias; //媒体数据，图片或小视频 url
    private Integer seeType; // 谁可以看，1-公开，2-私密，3-部分可见，4-不给谁看

    private String longitude; //经度
    private String latitude; //纬度
    private String locationName; //位置名称
    private String created; //发布时间

    private Integer likeCount = 0; //点赞数
    private Integer commentCount = 0; //评论数
    private Integer loveCount = 0; //喜欢数
}
