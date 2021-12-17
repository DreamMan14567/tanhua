package com.tanhua.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.interceptors.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/17
 * @Description
 */
@Service
@Slf4j
public class UserLikeService {
    @Reference
    private UserLikeApi userLikeApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 统计互相喜欢 喜欢 粉丝
     *
     * @return
     */
    public CountsVo count() {
        Long userId = UserHolder.getId();
        // 通过查询user_friend表来获得互相喜欢的人总数
        Long eachLoveCount = userLikeApi.countLoveEach(userId);
        // 通过查询follower 表来获取喜欢的人数
        Long loveCount = userLikeApi.countLove(userId);
        // 通过查询follower 表来获取粉丝人数
        Long fanCount = userLikeApi.countFans(userId);

        CountsVo vo = new CountsVo();
        vo.setEachLoveCount(eachLoveCount);
        vo.setLoveCount(loveCount);
        vo.setFanCount(fanCount);

        return vo;
    }

    /**
     * 分页查询喜欢关注看过列表
     *
     * @param page
     * @param pagesize
     * @param type
     * @return
     */
    public PageResult<FriendVo> findByPage4Type(Long page, Long pagesize, int type) {
        Long loginUserId = UserHolder.getId();
        PageResult pageResult = new PageResult();
        Boolean alreadyLove = false;
        switch (type) {
            case 1:
                // 查询 user_friend  条件: userId = userId 返回：朋友结果集
                pageResult = userLikeApi.findPage4EachLike(page, pagesize, loginUserId);
                alreadyLove = true;
                break;
            case 2:
                // 查询 follow_user: 条件: userId = userId
                pageResult = userLikeApi.findPage4Like(page, pagesize, loginUserId);
                alreadyLove = true;
                break;
            case 3:
                // 查询 follow_user: 条件: userId = followUserId
                pageResult = userLikeApi.findPage4Fans(page, pagesize, loginUserId);
                alreadyLove = false;
                break;
            case 4:
                // 查询visitor表： 条件：userId=userId
                pageResult = userLikeApi.findPage4Watch(page, pagesize, loginUserId);
                alreadyLove = false;
            default:
                break;
        }
        // 补全用户信息 RecommendUser
        // 获取分页结果集
        List<RecommendUser> recommendUserList = pageResult.getItems();
        // 遍历
        if (CollectionUtils.isNotEmpty(recommendUserList)) {
            List<FriendVo> voList = new ArrayList<>();
            for (RecommendUser recommendUser : recommendUserList) {
                // 获取用户的id 对方
                Long userId = recommendUser.getUserId();
                // 查询用户信息
                //TODO 这里有问题
                UserInfo userInfo = userInfoApi.findUserInfoById(userId);
                if (userInfo != null) {
                    log.info("userinfo:{}", userInfo);
                    // 转成vo
                    FriendVo vo = new FriendVo();
                    BeanUtils.copyProperties(userInfo, vo);
                    vo.setAge(Integer.valueOf(userInfo.getAge()));
                    vo.setAlreadyLove(alreadyLove);
                    // 缘分值
                    vo.setMatchRate(recommendUser.getScore().intValue());
                    voList.add(vo);

                }
            }
            // 设置回分页结果集
            pageResult.setItems(voList);
        }
        // 返回分页结果集
        return pageResult;
    }

    public void fansLike(Long userId) {
        Long loginUserId = UserHolder.getId();
        boolean isLike = userLikeApi.fansLike(userId,loginUserId);
        if (isLike) {
            huanXinTemplate.makeFriends(loginUserId,userId);
        }
    }

}
