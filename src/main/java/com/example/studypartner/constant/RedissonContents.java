package com.example.studypartner.constant;

/**
 * @author wuxie
 * @date 2023/11/20 19:40
 * @description redis缓存键
 */
public interface RedissonContents {

	String PRECACHE_JOB_KEY = "partner:precache:lock";
	String DELETE_EXPIRED_TEAMS_JOB_KEY = "partner:disbandteam:lock";

	String APPLY_LOCK = "partner:friend:apply:lock";
}
