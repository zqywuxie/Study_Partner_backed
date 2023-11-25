package com.example.studypartner.config;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class ChatConfig {
 
    public static ConcurrentHashMap<String, Channel> concurrentHashMap = new ConcurrentHashMap();
 
 
}
