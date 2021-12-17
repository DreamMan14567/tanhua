package com.tanhua.domain.db;

import com.tanhua.domain.db.BasePojo;
import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */
@Data
public class Conclusion extends BasePojo {
    private  Long id;
    private String conclusion;
    private String cover;
}
