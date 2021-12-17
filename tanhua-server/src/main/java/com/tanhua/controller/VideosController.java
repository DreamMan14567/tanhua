package com.tanhua.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author user_Chubby
 * @date 2021/5/16
 * @Description
 */
@RestController
@RequestMapping("/smallVideos")
public class VideosController {
    @Autowired
    private VideoService videoService;

    /**
     * 发送小视频
     *
     * @param videoThumbnail
     * @param videoFile
     * @return
     */
    @PostMapping
    public ResponseEntity postVideos(MultipartFile videoThumbnail, MultipartFile videoFile) {
        videoService.uploadVideo(videoThumbnail, videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 小视频列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping
    public ResponseEntity videoList(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize) {
        PageResult<VideoVo> pageResult = videoService.findPage(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 关注用户
     *
     * @param userId
     * @return
     */
    @PostMapping("/{userId}/userFocus")
    public ResponseEntity followUser(@PathVariable Long userId) {
        videoService.followUser(userId);
        return ResponseEntity.ok(null);
    }


    @GetMapping("/{userId}/userUnFocus")
    public ResponseEntity cancelFollow(@PathVariable Long userId){
        videoService.cancelFollow(userId);
        return ResponseEntity.ok(null);
    }
}
