package com.tanhua.manage.controller;

import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.service.AnalysisService;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author user_Chubby
 * @date 2021/5/18
 * @Description
 */

@RestController
@RequestMapping("/dashboard")
public class AnalysisController {
    @Autowired
    private AnalysisService analysisService;


    @GetMapping("/summary")
    public ResponseEntity getSummary(){
        AnalysisSummaryVo vo = analysisService.getSummary();
        return ResponseEntity.ok(vo);
    }

    @GetMapping("/users")
    public ResponseEntity getUsersCount(@RequestParam("sd") Long startDate,@RequestParam("ed") Long endDate,@RequestParam("type") Integer type){
        AnalysisUsersVo analysisUsersVo = analysisService.getUsersCount(startDate,endDate,type);
        return ResponseEntity.ok(analysisUsersVo);
    }
}
