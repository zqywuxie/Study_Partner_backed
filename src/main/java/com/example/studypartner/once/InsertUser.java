package com.example.studypartner.once;

import com.example.studypartner.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;



@Component
public class InsertUser {
    @Resource
    private UserMapper userMapper;

    /**
     * 定时任务
     */
//    @Scheduled(initialDelay = 5000,fixedDelay = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int sum = 10;
        System.out.println(sum);
//        for (int i = 0; i < sum; i++) {
//            User user = new User();
//            user.setUsername("无邪苦");
//            user.setUserAccount("wuxie");
//            user.setAvatarUrl("https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/5339/5e926a2bb7773958.jpg");
//            user.setGender(1);
//            user.setUserPassword("123456");
//            user.setEmail("123");
//            user.setPhone("123");
//            user.setUserRole(0);
//            user.setCity("dada");
//            user.setProfile("dad");
//            user.setProvince("dada");
//            user.setTags("[]");
//            userMapper.insert(user);
//
//        }
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}
