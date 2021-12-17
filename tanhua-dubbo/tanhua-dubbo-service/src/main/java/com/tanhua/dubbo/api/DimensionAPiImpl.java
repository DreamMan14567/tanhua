package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Dimension;
import com.tanhua.domain.db.Result;
import com.tanhua.dubbo.mapper.DimensionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 刘春林
 * @date 2021/5/25
 * @Description
 */
@Service
public class DimensionAPiImpl implements DimensionAPi {
    @Autowired
    private DimensionMapper dimensionMapper;

    @Override
    public void insertDimension(Dimension dimension) {
        dimensionMapper.insertInMyWay(dimension);
    }

}
