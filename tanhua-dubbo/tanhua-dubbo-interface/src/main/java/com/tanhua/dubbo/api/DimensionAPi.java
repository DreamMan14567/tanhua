package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Dimension;
import com.tanhua.domain.db.Result;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author 刘春林
 * @date 2021/5/25
 * @Description
 */
public interface DimensionAPi {
    void insertDimension(Dimension dimension);



}
