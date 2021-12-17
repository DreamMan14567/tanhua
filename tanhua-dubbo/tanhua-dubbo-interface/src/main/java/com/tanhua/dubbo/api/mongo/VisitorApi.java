package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Visitor;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */
public interface VisitorApi {
    /**
     * 查询来访记录
     * @param userId
     * @param lastTime
     * @return
     */
    List<Visitor> queryVisitor(Long userId, String lastTime);

    void save(Visitor visitor);
}
