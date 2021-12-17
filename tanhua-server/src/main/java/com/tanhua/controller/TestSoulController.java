package com.tanhua.controller;

import com.tanhua.domain.db.Answer;
import com.tanhua.domain.vo.FinalConclusionVo;
import com.tanhua.domain.vo.TestPageVo;
import com.tanhua.service.TestSoulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */

@RestController
@RequestMapping("/testSoul")
public class TestSoulController {
    @Autowired
    private TestSoulService testSoulService;

    @GetMapping("/report/{reportId}")
    public ResponseEntity getReport(@PathVariable Long reportId){

        FinalConclusionVo vo = testSoulService.getReport(reportId);
        System.out.println(vo);
        return ResponseEntity.ok(vo);
    }

    @GetMapping
    public ResponseEntity getTestPage(){
        List<TestPageVo> testPageVoList = testSoulService.getTestPage();
        return ResponseEntity.ok(testPageVoList);
    }



    @PostMapping
    public ResponseEntity submitPage(@RequestBody Map<String,List<Answer>> answers){
        String reportId = testSoulService.submit(answers);
        return ResponseEntity.ok(reportId);
    }
}
