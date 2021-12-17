package com.tanhua;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.tanhua.commons.templates.FaceRecTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.dubbo.api.mongo.LocationApi;
import com.tanhua.dubbo.api.mongo.VisitorApi;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author user_Chubby
 * @date 2021/5/4
 * @Description
 */

@SpringBootTest(classes = TanhuaServerApplication.class)
@RunWith(SpringRunner.class)
public class TestDemo {
    @Reference
    private LocationApi locationApi;

    @Reference
    private VisitorApi visitorApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FaceRecTemplate faceRecTemplate;

    @Test
    public void testFileUpload() {
        FileInputStream is = null;
        try {
            is = new FileInputStream("C:\\Users\\user_Chubby\\Desktop\\zhm2.jpg");
            ossTemplate.upload("zhm2.jpg", is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFaceRec() {
        try {
            boolean detectionStructure = faceRecTemplate.detect(Files.readAllBytes(new File("C:\\Users\\user_Chubby\\Desktop\\zhm2.jpg").toPath()));
            System.out.println("检测结果：" + detectionStructure);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        Integer num = 4;
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        List<Integer> list1 = list.stream().filter(x -> x < num).collect(Collectors.toList());
        list1.forEach(System.out::println);
    }

    @Test
    public void testSave() {
        for (int i = 0; i < 10; i++) {
            Visitor visitor = new Visitor();
            visitor.setFrom("首页");
            visitor.setUserId(1L);//用户id
            visitor.setVisitorUserId(RandomUtils.nextLong(1, 20));
            this.visitorApi.save(visitor);
        }
        System.out.println("ok");
    }

    @Test
    public void test01() {
        this.locationApi.addLocation(113.929778,22.582111,"深圳黑马程序员",1l);
        this.locationApi.addLocation(113.925528,22.587995,"红荔村肠粉",2l);
        this.locationApi.addLocation(113.93814,22.562578,"深圳南头直升机场",3l);
        this.locationApi.addLocation(114.064478,22.549528,"深圳市政府",4l);
        this.locationApi.addLocation(113.986074,22.547726,"欢乐谷",5l);
        this.locationApi.addLocation(113.979399,22.540746,"世界之窗",6l);
        this.locationApi.addLocation(114.294924,22.632275,"东部华侨城",7l);
        this.locationApi.addLocation(114.314011,22.598196,"大梅沙海滨公园",8l);
        this.locationApi.addLocation(113.821705,22.638172,"深圳宝安国际机场",9l);
        this.locationApi.addLocation(113.912386,22.566223,"海雅缤纷城(宝安店)",10l);
    }

    
}
