package com.tanhua.service;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.RecommendUserVo;
import com.tanhua.domain.vo.UserInfoAgeVo;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.interceptors.UserHolder;
import com.tanhua.utils.GetAgeUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */

@Service
public class RecommendUserService {
    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Reference
    private SettingApi settingApi;

    /**
     * 获取今日佳人
     *
     * @return
     */
    public RecommendUserVo todayBest() {
        //
        Long loginUserId = UserHolder.getId();
        // 获取今日最佳
        RecommendUser recommendUser = recommendUserApi.queryBestByScore(loginUserId);
        if (null == recommendUser) {
            //给一个默认的客户
            recommendUser = new RecommendUser();
            recommendUser.setUserId(1L);
            recommendUser.setScore(75d);

        }
        // 属性复制
        RecommendUserVo vo = new RecommendUserVo();
        // 用户信息补充
        UserInfo recommendUserInfo = userInfoApi.findUserInfoById(recommendUser.getUserId());
        // 用户信息复制
        BeanUtils.copyProperties(recommendUserInfo, vo);
        // 用户信息补充
        vo.setId(recommendUserInfo.getId());
        vo.setAge(GetAgeUtil.getAge(recommendUserInfo.getBirthday()));
        vo.setTags(StringUtils.split(recommendUserInfo.getTags(), ","));
        vo.setFateValue(recommendUser.getScore().intValue());
        // 返回今日最佳对象
        return vo;

    }

    /**
     * 分页缘分值降序查询推荐好友列表
     *
     * @param queryParam
     * @return
     */
    public PageResult<RecommendUserVo> queryRecommendation(RecommendUserQueryParam queryParam) {
        long page = queryParam.getPage();
        long pagesize = queryParam.getPagesize();
        Long userId = UserHolder.getId();
        // 调用远程API查询RecommendUser对象列表   降序 分页
        PageResult pageResult = recommendUserApi.queryRecommendUser(page, pagesize, userId);
        // 获取对象列表
        List<RecommendUser> recommendUserList = pageResult.getItems();

        if (CollectionUtils.isEmpty(recommendUserList)) {
            recommendUserList = getDefaultRecommendUser();
        }


        // 通过对象列表获取所有的用户ID
        List<Long> userIds = recommendUserList.stream().map(RecommendUser::getUserId).collect(Collectors.toList());
        // 需要不全用户信息，查询用户详情列表
        List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(userIds);
        Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));
        // 在封装为 RecommendUserVo进行返回
        List<RecommendUserVo> voList = recommendUserList.stream().map(recommendUser -> {
            RecommendUserVo vo = new RecommendUserVo();
            //复制用户详情信息
            UserInfo userInfo = userInfoMap.get(recommendUser.getUserId());
            BeanUtils.copyProperties(userInfo, vo);
            // 用户详情补充
            vo.setAge(Integer.valueOf(userInfo.getAge()));
            vo.setTags(StringUtils.split(userInfo.getTags(), ","));
            // 复制推荐用户信息
            BeanUtils.copyProperties(recommendUser, vo);
            // 信息补充
            vo.setFateValue(recommendUser.getScore().intValue());
            return vo;
        }).collect(Collectors.toList());
        // 通过ID列表获取用户信息，进行属性复制
        pageResult.setItems(voList);

        return pageResult;
    }

    /**
     * 默认的客服
     *
     * @return
     */
    private List<RecommendUser> getDefaultRecommendUser() {
        List<RecommendUser> list = new ArrayList<RecommendUser>();
        for (long i = 1; i < 10; i++) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(i);
            // 产生随机分数，70-98之间的
            recommendUser.setScore(RandomUtils.nextDouble(70, 98));
            list.add(recommendUser);
        }
        return list;
    }

    public RecommendUserVo queryBestInfo(Long userId) {
        Long loginUserId = UserHolder.getId();
        // 调用API获得对应的recommendUser
        RecommendUser recommendUser = recommendUserApi.queryBestInfo(userId, loginUserId);

        UserInfo userInfo = userInfoApi.findUserInfoById(userId);
        // 构建一个vo对象
        RecommendUserVo vo = new RecommendUserVo();
        // 封装为recommendUserVo
        if (null != recommendUser) {
            // 复制用户信息
            BeanUtils.copyProperties(userInfo, vo);
            vo.setTags(StringUtils.split(userInfo.getTags(), ","));
            vo.setAge(Integer.valueOf(userInfo.getAge()));
            // 复制佳人信息
            vo.setFateValue(recommendUser.getScore().intValue());
            if (recommendUser.getScore() == null) {
                vo.setFateValue(75);
            }
        }
        return vo;
    }

    /**
     * 查看陌生人问题
     *
     * @param userId
     * @param context
     * @return
     */
    public String queryStrangerQuestion(Long userId, String context) {
        // 通过userId可以获取Question对象
        Question question = questionApi.queryQuestion(userId);
        // 通过question获取问题进行返回
        String strangerQuestion = question.getTxt();
        // 返回之前需要进行判断，判断是否有问题
        // 有则返回txt
        if (StringUtils.isEmpty(strangerQuestion)) {
            // 无则给一个默认问题
            strangerQuestion = "约吗";
        }
        return strangerQuestion;
    }

    /**
     * 回复陌生人问题
     *
     * @param param
     */
    public void answerQuestion(Map<String, Object> param) {
        String reply = (String) param.get("reply");
        Long userId = Long.valueOf((Integer) param.get("userId"));
        //湖区用户信息，为传输信息做准备
        UserInfo userInfo = userInfoApi.findUserInfoById(UserHolder.getId());
        Question question = questionApi.queryQuestion(userId);

        //构建消息内容
        Map<String, String> map = new HashMap<String, String>();
        map.put("userId", userInfo.getId().toString());
        map.put("nickname", userInfo.getNickname());
        map.put("strangerQuestion", question == null ? "你喜欢我吗？" : question.getTxt());
        map.put("reply", reply);

        String msg = JSON.toJSONString(map);
        huanXinTemplate.sendMsg(userId + "", msg);
    }

    public List queryCards() {
        Long loginUserId = UserHolder.getId();
        // 查询用户信息返回即可
        List<Long> ids = new ArrayList<>();
        /*for (int i = 0; i < 10; i++) {
            long id = RandomUtils.nextLong(0, 1000);
            // 判断我是否已经喜欢他或者已经是好友，如果是i = i-1
            long id = RandomUtils.nextLong(0, 1000);
        }*/
        //添加非黑名单非好友非喜欢人列表
        while (ids.size() != 10) {
            long id = RandomUtils.nextLong(0, 1000);
            if (!recommendUserApi.findMyLove(id,loginUserId) && !recommendUserApi.findMyHate(id, loginUserId)) {
                ids.add(id);
            }
        }
        List<UserInfo> userInfoList = userInfoApi.findUSerByBatchId(ids);

        List<RecommendUserVo> voList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {

            RecommendUserVo vo = new RecommendUserVo();
            BeanUtils.copyProperties(userInfo, vo);
            vo.setTags(StringUtils.split(userInfo.getTags(), ","));
            vo.setAge(Integer.valueOf(userInfo.getAge()));
            voList.add(vo);
        }
        return voList;
    }

    public void loveYou(Long userId) {
        Long loginUserId = UserHolder.getId();
        // 判断喜欢的人是否已经喜欢我
        Boolean isLoveMe = recommendUserApi.isLoveMe(userId, loginUserId);

        if (isLoveMe) {
            recommendUserApi.loveEachOther(loginUserId, userId);
        } else {
            recommendUserApi.justIlove(loginUserId, userId);
        }
    }

    public void unloveYou(Long uid) {
        Long loginUserId = UserHolder.getId();
        // 添加到黑名单中
        recommendUserApi.addBlacklist(loginUserId,uid);
    }
}
