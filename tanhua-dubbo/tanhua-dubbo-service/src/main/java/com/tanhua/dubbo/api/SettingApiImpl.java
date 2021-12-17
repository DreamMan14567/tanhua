package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlackListMapper;
import com.tanhua.dubbo.mapper.SettingsMapper;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
@Service
public class SettingApiImpl implements SettingApi {
    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private BlackListMapper blackListMapper;

    /**
     * 查询用户陌生人问题
     *
     * @param userId
     * @return
     */
    @Override
    public Settings queryUserById(Long userId) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return settingsMapper.selectOne(queryWrapper);
    }

    /**
     * 添加通知
     *
     * @param settings
     */
    @Override
    public void insertSetting(Settings settings) {
        settingsMapper.insert(settings);
    }

    /**
     * 更新通知
     *
     * @param settings
     */
    @Override
    public void updateSetting(Settings settings) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", settings.getUserId());
        settingsMapper.update(settings, queryWrapper);
    }

    /**
     * 分页查询黑名单
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findBlackListPage(Long userId, Long page, Long pagesize) {
        // 分页对象
        IPage<BlackList> ipage = new Page<BlackList>(page, pagesize);
        // 查询条件
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        blackListMapper.selectPage(ipage, queryWrapper);

        PageResult<BlackList> pageResult = new PageResult<>();
        pageResult.setPage(page); // 当前的页码
        pageResult.setPagesize(pagesize); // 每页大小
        pageResult.setItems(ipage.getRecords()); // 分页结果集
        pageResult.setCounts(ipage.getTotal()); //  总记录数
        pageResult.setPages(ipage.getPages());// 总页数
        return pageResult;
    }

    /**
     * 移除黑名单
     *
     * @param blackList
     */
    @Override
    public void deleteBlacker(BlackList blackList) {
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("black_user_id", blackList.getBlackUserId());
        queryWrapper.eq("user_id", blackList.getUserId());
        blackListMapper.delete(queryWrapper);
    }
}
