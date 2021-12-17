package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */

@Service
public class UserInfoApiImpl implements UserInfoApi {
    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 新用户---1填写资料
     *
     * @param userInfo 用户数据信息
     * @return
     */
    @Override
    public void fillUserInformation(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    /**
     * 对头像进行选取并更新
     *
     * @param userInfo
     */
    @Override
    public void updateAvatar(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userInfo.getId());
        userInfoMapper.update(userInfo, queryWrapper);
    }

    /**
     * 根据ID查找用户信息
     *
     * @param loginUserId
     * @return
     */
    @Override
    public UserInfo findUserInfoById(Long loginUserId) {
        return userInfoMapper.selectById(loginUserId);
    }

    /**
     * 用户资料 - 保存
     *
     * @param userInfo
     */
    @Override
    public void update(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userInfo.getId());
        userInfoMapper.update(userInfo, queryWrapper);
    }

    /**
     * 批量查询用户
     *
     * @param blackListIds
     * @return
     */
    @Override
    public List<UserInfo> findUSerByBatchId(List<Long> blackListIds) {
        return userInfoMapper.selectBatchIds(blackListIds);
    }

    /**
     * 分页查询用户详情信息
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPage(Long page, Long pagesize) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        IPage<UserInfo> infoIPage = userInfoMapper.selectPage(new Page<>(page, pagesize), queryWrapper);
        List<UserInfo> userInfoList = infoIPage.getRecords();
        return new PageResult(infoIPage.getTotal(), pagesize, infoIPage.getPages(), page, userInfoList);
    }
}
