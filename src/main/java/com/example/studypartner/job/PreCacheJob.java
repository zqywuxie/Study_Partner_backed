package com.example.studypartner.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.domain.User;
import com.example.studypartner.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    //重点用户
    private List<Long> mainUsers = Arrays.asList(4L);

    @Scheduled(cron = "0 0 0 * * * ")
    public void doCacheRecommendUser() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Page<User> page = userService.page(new Page<>(1, 5), userQueryWrapper);
        String rediskey = String.format("wuxie:user:recommend:%s", mainUsers);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        try {
            valueOperations.set(rediskey, page, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("PreCache is error", e);
        }
    }

}
