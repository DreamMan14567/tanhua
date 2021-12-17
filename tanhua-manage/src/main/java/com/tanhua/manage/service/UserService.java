package com.tanhua.manage.service;

import cn.hutool.core.collection.CollectionUtil;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Moment;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.MomentApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.manage.domain.MomentDateStrVo;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/19
 * @Description
 */

@Service
public class UserService {
    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private VideoApi videoApi;

    @Reference
    private MomentApi momentApi;

    @Reference
    private CommentApi commentApi;


    /**
     * 所有用户信息分页显示
     *
     * @return
     */
    public PageResult<UserInfoVo> findPage(Long page, Long pagesize) {
        PageResult pageResult = userInfoApi.findPage(page, pagesize);

        List<UserInfo> userInfoList = pageResult.getItems();

        if (CollectionUtil.isNotEmpty(userInfoList)) {
            List<UserInfoVo> voList = new ArrayList<>();
            // 进行封装
            for (UserInfo userInfo : userInfoList) {
                UserInfoVo vo = new UserInfoVo();
                BeanUtils.copyProperties(userInfo, vo);
                voList.add(vo);
            }
            pageResult.setItems(voList);
        }
        return pageResult;
    }


    public UserInfo findById(long userId) {
        // 按照用户ID调用远程API进行查询
        // 查询出一个userinfo 返回即可
        return userInfoApi.findUserInfoById(userId);
    }

    /**
     * 查询视频记录
     *
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    public PageResult<Video> findVideos(Long page, Long pagesize, Long uid) {
        return videoApi.findUserVideo(page, pagesize, uid);
    }

    /**
     * 查询指定用户指定状态的动态
     *
     * @param page
     * @param pagesize
     * @param uid
     * @param state
     * @return
     */
    public PageResult<MomentDateStrVo> findAllMovements(Long page, Long pagesize, Long uid, int state) {
        PageResult pageResult = momentApi.queryUserStateMoment(page, pagesize, uid, state);
        List<Moment> momentList = pageResult.getItems();

        List<MomentDateStrVo> voList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(momentList)) {
            for (Moment moment : momentList) {
                MomentDateStrVo vo = new MomentDateStrVo();
                BeanUtils.copyProperties(moment, vo);
                vo.setCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(moment.getCreated())));
                voList.add(vo);
            }
            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 查询单挑动态
     *
     * @param publishId
     * @return
     */
    public Moment findMovementById(String publishId) {
        return momentApi.findById(publishId);
    }

    public PageResult findAllComments(Long page, Long pagesize, String messageID) {
        return commentApi.findPage(page,pagesize,messageID);
    }
}
