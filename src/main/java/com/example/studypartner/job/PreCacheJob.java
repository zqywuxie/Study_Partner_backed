package com.example.studypartner.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.studypartner.constant.RedissonContents.PRECACHE_JOB_KEY;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 指定角色的预热
     */
    private List<Long> mainUsers = Arrays.asList(4L);

    @Scheduled(cron = "0 0 0 * * * ")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock(PRECACHE_JOB_KEY);
        try {
            //-1 默认值表示不会过期
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                for (Long mainUser : mainUsers) {
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    Page<User> page = userService.page(new Page<>(1, 5), userQueryWrapper);
                    String rediskey = String.format("wuxie:user:recommend:%s", mainUser);
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(rediskey, page, 30, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("PreCache is error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser is error", e);
        } finally {
            System.out.println("hello");
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }

}
