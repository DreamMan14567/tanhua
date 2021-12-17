package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Conclusion;
import com.tanhua.dubbo.mapper.ConclusionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 刘春林
 * @date 2021/5/25
 * @Description
 */
@Service
public class ConclusionApiImpl implements ConclusionApi {
    @Autowired
    private ConclusionMapper conclusionMapper;

    @Override
    public Conclusion getConclusion(int i) {
        return conclusionMapper.selectById(i);
    }
}
