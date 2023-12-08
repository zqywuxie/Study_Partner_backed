package com.example.studypartner.service;

import com.example.studypartner.domain.entity.User;
import com.example.studypartner.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUserTest {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(12, 1000, 1000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000));


    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int sum = 1000;
        System.out.println(sum);
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < sum; i++) {
            User user = new User();
            user.setUsername("无邪苦");
            user.setTags("[]");
            users.add(user);
        }
        userService.saveBatch(users, 20);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }

    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int sum = 1000;
        int j = 0;
        ArrayList<CompletableFuture> completableFutures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ArrayList<User> users = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("无邪苦");
                user.setTags("[]");
                users.add(user);
                if (j % 50 == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(users, 50);
            }, executorService);
            completableFutures.add(future);
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}