package com.tanhua.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exceptions.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.interceptors.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/16
 * @Description
 */

@Service
public class VideoService {
    @Reference
    private VideoApi videoApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 上传小视频
     *
     * @param videoThumbnail
     * @param videoFile
     */
    public void uploadVideo(MultipartFile videoThumbnail, MultipartFile videoFile) {
        Long userId = UserHolder.getId();
        try {
            // 先将视频的封面上传到阿里云
            String filename = videoThumbnail.getOriginalFilename();
            InputStream is = videoThumbnail.getInputStream();
            // 获取上传之后的路径
            String coverUrl = ossTemplate.upload(filename, is);
            // 上传小视频到fastDFS
            String videoName = videoFile.getOriginalFilename();
            String videoSuffix = videoName.substring(videoName.lastIndexOf(".") + 1);

            StorePath storePath = fastFileStorageClient.uploadFile(videoFile.getInputStream(), videoFile.getSize(), videoSuffix, null);
            // 完整文件名
            String videoUrl = fdfsWebServer.getWebServerUrl() + storePath;
            // 创建一个Video对象
            Video video = new Video();
            video.setPicUrl(coverUrl);
            video.setVideoUrl(videoUrl);
            video.setText("宇宙大美女");
            video.setUserId(userId);
            // 封装video对象，调用远程API进行添加
            videoApi.save(video);
        } catch (IOException e) {
            throw new TanHuaException("上传失败");
        }
    }

    /**
     * 分页查询视频列表
     *
     * @return
     */
    public PageResult<VideoVo> findPage(Long page, Long pagesize) {
        // 获取video列表
        PageResult pageResult = videoApi.findPage(page, pagesize);
        List<Video> videoList = pageResult.getItems();

        if (CollectionUtils.isNotEmpty(videoList)) {
            List<Long> userIds = videoList.stream().map(Video::getUserId).collect(Collectors.toList());
            // 获取所需要的属性复制对象
            List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(userIds);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userinfo -> userinfo));
            // 对video对象进行封装——->videoVo
            List<VideoVo> voList = videoList.stream().map(video -> {
                VideoVo vo = new VideoVo();
                UserInfo userInfo = userInfoMap.get(video.getUserId());
                BeanUtils.copyProperties(userInfo, vo);
                BeanUtils.copyProperties(video, vo);
                vo.setCover(video.getPicUrl());
                vo.setId(video.getId().toHexString());
                if (StringUtils.isNotEmpty(video.getText())) {
                    vo.setSignature(video.getText());//签名
                } else {
                    vo.setSignature("默认签名");//签名
                }
                vo.setHasFocus(0); //TODO 是否关注
                vo.setHasLiked(0); //是否点赞
                return vo;
            }).collect(Collectors.toList());

            pageResult.setItems(voList);
            // 放进分页对象进行返回
        }
        return pageResult;
    }

    /**
     * 关注用户
     *
     * @param userId
     */
    public void followUser(Long userId) {
        Long loginUserId = UserHolder.getId();
        // 关注用户，意味着向关注表中添加一条记录
        // 构建一个follower对象
        FollowUser followUser = new FollowUser();
        // 补充属性
        followUser.setUserId(loginUserId);
        followUser.setFollowUserId(userId);
        // 调用远程API进行添加
        videoApi.follow(followUser);
        String key = "video_follow_" + loginUserId + "_" + followUser.getFollowUserId();
        redisTemplate.opsForValue().set(key, 1);
    }

    public void cancelFollow(Long userId) {
        Long loginUserId = UserHolder.getId();
        // 构建对象
        FollowUser followUser = new FollowUser();
        followUser.setUserId(loginUserId);
        followUser.setFollowUserId(userId);
        // 调用API进行删除
        videoApi.cancelFollow(followUser);
        // 删除REDIS中的值
        String key = "video_follow_" + loginUserId + "_" + followUser.getFollowUserId();
        redisTemplate.delete(key);
    }
}
