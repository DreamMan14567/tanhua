package com.tanhua.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.IMApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */

@Slf4j
@Service
public class IMService {
    @Reference
    private IMApi imApi;

    @Reference
    private CommentApi commentApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private FriendApi friendApi;


    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 获取公告列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult getAnnouncementList(Long page, Long pagesize) {
        // 获取公告列表页面
        PageResult pageResult = imApi.queryAnnouncement(page, pagesize);
        // 获取公告对象列表
        List<Announcement> announcementList = pageResult.getItems();
        // 判断是否为空
        if (CollectionUtils.isNotEmpty(announcementList)) {
            // 对象转换
            List<AnnouncementVo> voList = announcementList.stream().map(announcement -> {
                AnnouncementVo vo = new AnnouncementVo();
                // 属性复制
                BeanUtils.copyProperties(announcement, vo);
                return vo;
            }).collect(Collectors.toList());
            pageResult.setItems(voList);
        }
        log.info("pageResult:{}", pageResult);
        return pageResult;
    }

    public void addLinkman(Long userId) {
        // 获取当前用户ID
        Long loginUserId = UserHolder.getId();
        // 调用API进行添加
        imApi.addFriend(loginUserId, userId);
        // 环信进行添加好友
        huanXinTemplate.makeFriends(loginUserId, userId);
    }

    /**
     * 统计列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MessageVo> getMessageList(Integer type, Long page, Long pagesize) {
        Long userId = UserHolder.getId();
        //被评论的用户:userId  comment.getTargetUserId = userId
        PageResult pageResult = commentApi.findByUserId(page, pagesize, type, userId);

        List<Comment> commentList = pageResult.getItems();

        if (pageResult.getItems().size() == 0 || CollectionUtils.isEmpty(commentList)) {
            return pageResult;
        }

        List<Long> commentUserIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());

        // 查询出userInfo
        List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(commentUserIds);
        Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

        //进行转换，comment ——> messageVo
        List<MessageVo> messageVoList = commentList.stream().map(comment -> {
            // 信息补充 userinfo comment
            MessageVo vo = new MessageVo();
            UserInfo userInfo = userInfoMap.get(comment.getUserId());
            BeanUtils.copyProperties(comment, vo);
            BeanUtils.copyProperties(userInfo, vo);
            vo.setId(comment.getId().toHexString());
            vo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
            return vo;
        }).collect(Collectors.toList());

        pageResult.setItems(messageVoList);
        return pageResult;
    }

    public PageResult<ContactVo> findPage(Long page, Long pagesize) {
        Long loginUserId = UserHolder.getId();

        PageResult pageResult = friendApi.findPage(page,pagesize,loginUserId);
        List<Friend> friendList = pageResult.getItems();

        List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
        List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(friendIds);
        Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

        // useinfo 的id复制到了contactVo的id
        List<ContactVo> voList = friendList.stream().map(friend -> {
            ContactVo vo = new ContactVo();
            UserInfo userInfo = userInfoMap.get(friend.getFriendId());
            BeanUtils.copyProperties(userInfo, vo);
            vo.setUserId(userInfo.getId().toString());
            vo.setAge(Integer.valueOf(userInfo.getAge()));
            return vo;
        }).collect(Collectors.toList());

        pageResult.setItems(voList);

        return pageResult;
    }
}
