package com.tanhua.controller;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.RecommendUserVo;
import com.tanhua.service.LocationService;
import com.tanhua.service.RecommendUserService;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/15
 * @Description
 */
@RestController
@RequestMapping("/tanhua")
public class RecommendedUserController {
    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private LocationService locationService;


    /**
     * 今日佳人
     *
     * @return
     */
    @GetMapping("/todayBest")
    public ResponseEntity todayBest() {
        RecommendUserVo vo = recommendUserService.todayBest();
        return ResponseEntity.ok(vo);
    }


    /**
     * 获取推荐列表--推荐朋友
     *
     * @param queryParam
     * @return
     */
    @GetMapping("/recommendation")
    public ResponseEntity recommendUserList(RecommendUserQueryParam queryParam) {
        PageResult<RecommendUserVo> pageResult = recommendUserService.queryRecommendation(queryParam);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查看佳人信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/{userId}/personalInfo")
    public ResponseEntity getBestInfo(@PathVariable Long userId) {
        RecommendUserVo vo = recommendUserService.queryBestInfo(userId);
        return ResponseEntity.ok(vo);
    }

    /**
     * 查看陌生人问题
     *
     * @param userId
     * @param context
     * @return
     */
    @GetMapping("/strangerQuestions")
    public ResponseEntity getStrangerQuestion(Long userId, String context) {
        String question = recommendUserService.queryStrangerQuestion(userId, context);
        return ResponseEntity.ok(question);
    }


    @PostMapping("/strangerQuestions")
    public ResponseEntity answerStrangerQuestion(@RequestBody Map<String,Object> param){
        recommendUserService.answerQuestion(param);
        return ResponseEntity.ok(null);
    }

    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity searchNearBy(@RequestParam(required=false) String gender,
                                       @RequestParam(defaultValue = "2000") String distance){
        List<NearUserVo> list = locationService.searchNearBy(gender,distance);
        return ResponseEntity.ok(list);
    }

    /**
     * 卡片左右滑
     * @return
     */

    @GetMapping("/cards")
    public ResponseEntity cards(){
        List getCards = recommendUserService.queryCards();
        return ResponseEntity.ok(getCards);
    }

    @GetMapping("/{uid}/love")
    public ResponseEntity loveYou(@PathVariable Long uid){
        recommendUserService.loveYou(uid);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{uid}/unlove")
    public ResponseEntity unloveYou(@PathVariable Long uid){
        recommendUserService.unloveYou(uid);
        return ResponseEntity.ok(null);
    }
}
