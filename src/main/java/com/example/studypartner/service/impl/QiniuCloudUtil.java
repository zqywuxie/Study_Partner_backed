package com.example.studypartner.service.impl;

import com.example.studypartner.service.QiniuCloud;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
  
/**
 * @author dsn
 * @createTime 07 21:07
 * @description 七牛云工具
 */

@Service
public class QiniuCloudUtil implements QiniuCloud {
  
    // 设置需要操作的账号的AK和SK
    private static final String ACCESS_KEY = "piD_u0yN3IMmdnKnCWkU9gMoLfNsF74Z2jgwWQVY";
    private static final String SECRET_KEY = "wc1C9KgnFcV9D1YmWCzaD3MAw99BF_x0KqiLetmv";
    // 要上传的空间名
    private static final String bucketname = "zqywuxie";
    private static final String domain = "https://zqywuxie.top";       //外链域名
    // 密钥
    private static final Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
  
//    //上传
    public  String upload(File file)  {
        // 创建上传对象，Zone*代表地区
        Configuration configuration = new Configuration(Zone.zone2());
        UploadManager uploadManager = new UploadManager(configuration);
        try {
            // 调用put方法上传
            String token = auth.uploadToken(bucketname);
            if(StringUtils.isEmpty(token)) {
                System.out.println("未获取到token，请重试！");
                return null;
            }
            String imageName = file.getName();
            System.out.println("File name = "+imageName);
            Response res = uploadManager.put(file,imageName,token);
            // 打印返回的信息
            if (res.isOK()){
                return imageName;
            }
        }catch (QiniuException e) {
            Response r = e.response;
            // 请求失败时打印的异常的信息
            e.printStackTrace();
            System.out.println("error "+r.toString());
            try {
                // 响应的文本信息
                System.out.println(r.bodyString());
            } catch (QiniuException e1) {
                System.out.println("error "+e1.error());
            }
        }
        return null;
    }
}