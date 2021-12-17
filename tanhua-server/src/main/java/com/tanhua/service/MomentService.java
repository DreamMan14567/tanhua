package com.tanhua.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.commons.exceptions.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Moment;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.dubbo.api.mongo.VisitorApi;
import com.tanhua.dubbo.api.mongo.MomentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.utils.RelativeDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */

@Service
public class MomentService {
    @Reference
    private VisitorApi visitorApi;

    @Reference
    private MomentApi momentApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布动态
     *
     * @param publishVo    动态传输对象
     * @param imageContent 动态传输文件
     */
    public void postMoment(PublishVo publishVo, MultipartFile[] imageContent) {
        Long loginUserId = UserHolder.getId();
        // 文件上传到阿里云oss
        List<String> medias = new ArrayList<>();
        try {
            if (imageContent != null) {
                for (MultipartFile file : imageContent) {
                    String imageUrl = ossTemplate.upload(file.getOriginalFilename(), file.getInputStream());
                    // 获取新的路径数组
                    medias.add(imageUrl);
                }
            }
        } catch (IOException e) {
            throw new TanHuaException("上传失败");
        }
        // 构建一个moment对象
        Moment moment = new Moment();
        // 属性复制
        BeanUtils.copyProperties(publishVo, moment);
        moment.setUserId(loginUserId);
        moment.setMedias(medias);
        moment.setSeeType(0);
        moment.setState(0);
        // 调用第三方API进行发布
        momentApi.add(moment);
    }

    /**
     * 查询好友动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MomentVo> queryFriendMoment(Long page, Long pagesize) {
        // 获取登录用户ID，通过该ID查询对应用户的时间线表
        Long loginUserId = UserHolder.getId();
        // 通过传过来的ID查询好友的所有动态
        PageResult pageResult = momentApi.queryFriendMoment(page, pagesize, loginUserId);
        // 获取动态列表
        List<Moment> momentList = pageResult.getItems();

        // 判空，只有当非空后续才能拿进行
        if (CollectionUtils.isNotEmpty(momentList)) {
            // 通过动态列表获取用户ID， 在通过用户ID们去查找所有用户详情信息，
            List<Long> ids = momentList.stream().map(Moment::getUserId).collect(Collectors.toList());
            // 通过用户IDS去查找所有的用户详情
            List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(ids);
            // 为了查找方便，把列表转换为map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

            //开始进行循环遍历，进行属性复制
            List<MomentVo> voList = momentList.stream().map(moment -> {
                // 构建vo对象
                MomentVo vo = new MomentVo();
                // 获取对应的信息
                UserInfo userInfo = userInfoMap.get(moment.getUserId());
                // 进行属性复制
                BeanUtils.copyProperties(moment, vo);
                vo.setId(moment.getId().toHexString());
                // 信息补充
                vo.setDistance("25米");
                vo.setCreateDate(RelativeDateFormat.format(new Date(moment.getCreated())));
                vo.setImageContent(moment.getMedias().toArray(new String[0]));
                // TODO 判断是否已经喜欢
                vo.setHasLiked(0);
                // TODO 判断是否推荐
                vo.setHasLoved(0);
                // 复制用户信息
                BeanUtils.copyProperties(userInfo, vo);
                vo.setAge(Integer.valueOf(userInfo.getAge()));
                vo.setTags(StringUtils.split(userInfo.getTags(), ","));
                return vo;
            }).collect(Collectors.toList());
            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 获取推荐动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MomentVo> getRecommendMoment(Long page, Long pagesize) {
        // 获取用户ID，以方便获取推荐给谁的动态
        Long loginUserId = UserHolder.getId();
        // 获取推荐的动态结果集
        PageResult pageResult = momentApi.queryRecommendMoment(page, pagesize, loginUserId);
        List<Moment> momentList = pageResult.getItems();

        if (CollectionUtils.isNotEmpty(momentList)) {
            // 获取用户详细信息
            List<Long> userIds = momentList.stream().map(Moment::getUserId).collect(Collectors.toList());
            List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(userIds);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));
            // 复制属性到动态对象上
            List<MomentVo> voList = momentList.stream().map(moment -> {
                MomentVo vo = new MomentVo();
                UserInfo userInfo = userInfoMap.get(moment.getUserId());
                // 进行属性复制
                BeanUtils.copyProperties(moment, vo);
                // 信息补充
                vo.setId(moment.getId().toHexString());
                vo.setDistance("25米");
                vo.setCreateDate(RelativeDateFormat.format(new Date(moment.getCreated())));
                vo.setImageContent(moment.getMedias().toArray(new String[0]));
                // TODO 判断是否已经喜欢
                vo.setHasLiked(0);
                // TODO 判断是否推荐
                vo.setHasLoved(0);
                // 复制用户信息
                BeanUtils.copyProperties(userInfo, vo);
                vo.setAge(Integer.valueOf(userInfo.getAge()));
                vo.setTags(StringUtils.split(userInfo.getTags(), ","));
                return vo;
            }).collect(Collectors.toList());
            // 封装为result进行返回
            pageResult.setItems(voList);
        }
        return pageResult;
    }

    public PageResult<MomentVo> getMyMoment(Long page, Long pagesize, Long userId) {
        // 获得moment的分页对象
        PageResult pageResult = momentApi.queryUserMoment(page, pagesize, userId);
        List<Moment> momentList = pageResult.getItems();
        // 通过用户ID查询对应的时间线表 -- api中实现
        // 通过时间线表 获取所有的动态ID -- api中实现

        if (CollectionUtils.isNotEmpty(momentList)) {
            // 获取用户详细信息
            List<Long> userIds = momentList.stream().map(Moment::getUserId).collect(Collectors.toList());
            // 通过动态ID获取所有的动态
            List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(userIds);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));
            // 对获取的对象进行遍历属性复制
            List<MomentVo> voList = momentList.stream().map(moment -> {
                MomentVo vo = new MomentVo();
                UserInfo userInfo = userInfoMap.get(moment.getUserId());
                // 进行属性复制
                BeanUtils.copyProperties(moment, vo);
                vo.setId(moment.getId().toHexString());
                // 信息补充
                vo.setDistance("25米");
                vo.setCreateDate(RelativeDateFormat.format(new Date(moment.getCreated())));
                vo.setImageContent(moment.getMedias().toArray(new String[0]));
                // TODO 判断是否已经喜欢
                vo.setHasLiked(0);
                // TODO 判断是否推荐
                vo.setHasLoved(0);
                // 复制用户信息
                BeanUtils.copyProperties(userInfo, vo);
                vo.setAge(Integer.valueOf(userInfo.getAge()));
                vo.setTags(StringUtils.split(userInfo.getTags(), ","));
                return vo;
            }).collect(Collectors.toList());
            // 封装为result进行返回
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
    public MomentVo findById(String publishId) {
        Moment moment = momentApi.findById(publishId);
        Long userId = moment.getUserId();
        String likeKey = "publish_like_" + userId + "_" + publishId;
        String loveKey = "publish_love_" + userId + "_" + publishId;
        //属性封装
        MomentVo vo = new MomentVo();
        UserInfo userInfo = userInfoApi.findUserInfoById(userId);
        // 进行属性复制
        BeanUtils.copyProperties(moment, vo);
        vo.setId(moment.getId().toHexString());
        // 信息补充
        vo.setDistance("25米");
        vo.setCreateDate(RelativeDateFormat.format(new Date(moment.getCreated())));
        vo.setImageContent(moment.getMedias().toArray(new String[0]));
        // TODO 判断是否已经喜欢
        vo.setHasLiked(0);
        if (redisTemplate.opsForValue().get(likeKey) != null) {
            vo.setHasLiked(1);
        }
        // TODO 判断是否推荐
        vo.setHasLoved(0);
        if (redisTemplate.opsForValue().get(loveKey) != null) {
            vo.setHasLoved(1);
        }
        // 复制用户信息
        BeanUtils.copyProperties(userInfo, vo);
        vo.setAge(Integer.valueOf(userInfo.getAge()));
        vo.setTags(StringUtils.split(userInfo.getTags(), ","));
        return vo;
    }

    /**
     * 谁看过我 -- 查询谁 访问 我
     *
     * @return
     */
    public List<VisitorVo> visit() {
        // 获取上一次访问的时间 --- redis
        Long userId = UserHolder.getId();
        String key = "visitors_time_" + userId;
        String lastTime = (String) redisTemplate.opsForValue().get(key);
        // 调用远程API查询visitor列表  访问时间  用户
        List<Visitor> visitorList = visitorApi.queryVisitor(userId, lastTime);
        // 进行属性封装
        List<VisitorVo> voList = new ArrayList<>();
        for (Visitor visitor : visitorList) {
            // 访客的id
            Long visitorUserId = visitor.getVisitorUserId();
            // 查询访客信息
            UserInfo visitorUserInfo = userInfoApi.findUserInfoById(visitorUserId);
            VisitorVo vo = new VisitorVo();
            BeanUtils.copyProperties(visitorUserInfo, vo);
            vo.setTags(StringUtils.split(visitorUserInfo.getTags(), ","));
            // 缘分值
            vo.setFateValue(visitor.getScore().intValue());
            voList.add(vo);
        }
        // 返回list
        return voList;
    }
}
