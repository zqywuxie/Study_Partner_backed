package com.example.studypartner.job;

import cn.hutool.core.date.StopWatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studypartner.domain.entity.Team;
import com.example.studypartner.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.studypartner.constant.RedissonContents.DELETE_EXPIRED_TEAMS_JOB_KEY;

/**
 * @author wuxie
 * @date 2023/11/20 19:39
 * @description 删除过期队伍job
 */

@Slf4j
public class DeleteExpiredTeamsJob extends QuartzJobBean {

	@Resource
	private TeamService teamService;

	@Resource
	private RedissonClient redissonClient;

	StopWatch stopWatch = new StopWatch();

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		RLock lock = redissonClient.getLock(DELETE_EXPIRED_TEAMS_JOB_KEY);
		try {
			//-1 默认值表示不会过期
			if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
				System.out.println("开始删除过期队伍");
				stopWatch.start();
				List<Team> list = teamService.list();
				for (Team team : list) {
					Date expireTime = team.getExpireTime();
					// 获取当前时间（以系统默认时区为准）
					Instant currentInstant = Instant.now();
					Date currentTime = Date.from(currentInstant);
					// 比较当前时间和过期时间
					if (currentTime.after(expireTime)) {
						teamService.deleteTeam(team.getId(), null);
					}
				}
				stopWatch.stop();
				log.info("删除过期队伍结束，花费时间为:{}", stopWatch.getTotalTimeSeconds());
			}
		} catch (InterruptedException e) {
			log.error("doCacheRecommendUser is error", e);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				System.out.println("unlock:" + Thread.currentThread().getId());
				lock.unlock();
			}
		}
	}
}
