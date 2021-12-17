package com.tanhua.domain.vo;

import com.tanhua.domain.db.BasePojo;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.db.Dimension;
import lombok.Data;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */
@Data
public class FinalConclusionVo extends BasePojo {
    private String conclusion;
    private String cover;
    private List<Dimension> dimensions;
    private List<UserInfo> similarYou;
}
