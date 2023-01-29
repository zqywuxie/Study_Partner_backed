package com.example.studypartner.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入数据到数据库
 */
public class ImportSQL {
    public static void main(String[] args) {
        String fileName = "/home/wuxie/桌面/Data.xlsx";
        List<UserInfo> list = EasyExcel.read(fileName).head(UserInfo.class).sheet().doReadSync();
        System.out.println("总数:"+list.size());
        //进行昵称去重
        Map<String, List<UserInfo>> collect = list.stream().filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                .collect(Collectors.groupingBy(userInfo -> userInfo.getUsername()));
        for (Map.Entry<String, List<UserInfo>> stringListEntry : collect.entrySet()) {
            if (stringListEntry.getValue().size()>1){
                System.out.println("重复的username:"+stringListEntry.getKey());
                System.out.println("1");
            }
        }
        System.out.println("不重名的昵称数"+collect.size());
    }
}
