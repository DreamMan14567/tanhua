package com.tanhua.service;

import com.tanhua.commons.exceptions.TanHuaException;
import com.tanhua.commons.templates.FaceRecTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.utils.GetAgeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author user_Chubby
 * @date 2021/5/14
 * @Description
 */

@Service
@Slf4j
public class UserInfoService {
    @Reference
    private UserInfoApi userInfoApi;


    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FaceRecTemplate faceRecTemplate;

    /**
     * 读取用户资料
     *
     * @return 用户数据运输类
     */
    public UserInfoVo readUserInfo() {
        // 直接从token中获取用户ID
        Long loginUserId = UserHolder.getId();
        // 通过用户ID调用userInfoApi进行查询
        UserInfo userInfo = userInfoApi.findUserInfoById(loginUserId);
        // 対回传过来的userInfo对象进行补充
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        userInfoVo.setMarriage(0);
        // 返回vo对象
        return userInfoVo;
    }

    /**
     * 用户资料的保存更新
     *
     * @param userInfoVo
     */
    public void save(UserInfoVo userInfoVo) {
        // 构建一个userinfo对象
        UserInfo userInfo = new UserInfo();
        // 进行属性复制
        BeanUtils.copyProperties(userInfoVo, userInfo);
        // 属性补充
        userInfo.setAge(String.valueOf(GetAgeUtil.getAge(userInfoVo.getBirthday())));
        userInfo.setId(userInfoVo.getId());
        // 调用远程API进行保存
        userInfoApi.update(userInfo);
    }

    public void updateAvatar(MultipartFile headPhoto) {
        User loginUser = UserHolder.getUser();
        try {
            // 存储新头像到阿里云存储
            // 判断头像是否符合规范
            boolean detect = faceRecTemplate.detect(headPhoto.getBytes());
            log.info("detect:{}", detect);
            if (!detect) {
                throw new TanHuaException(ErrorResult.faceError());
            }
            String avatarUrl = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
            // 通过id查询用户详情，获取用户的旧的头像地址，要删除
            UserInfo userInfoInDB = userInfoApi.findUserInfoById(loginUser.getId());
            // 旧头像
            String oldAvatar = userInfoInDB.getAvatar();
            // 更新头像
            UserInfo userInfo = new UserInfo();
            userInfo.setId(loginUser.getId());
            userInfo.setAvatar(avatarUrl);
            userInfoApi.update(userInfo);
            // 删除旧头像
            //https://sztanhua.oss-cn-shenzhen.aliyuncs.com/avatar/woman/14.jpg
            ossTemplate.deleteSingleFile(oldAvatar);
            log.info("newAvatar:{}", avatarUrl);
            // 调用远程API更新用户头像信息
            userInfoApi.update(userInfo);
            log.info("userinfo:{}", userInfo);
            // 删除旧头像
            ossTemplate.deleteSingleFile(oldAvatar);
        } catch (IOException e) {
            throw new TanHuaException("上传失败");
        }
    }


}
