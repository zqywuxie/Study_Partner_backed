package com.example.studypartner;

import com.example.studypartner.mapper.UserMapper;
import com.example.studypartner.service.OssService;
import com.example.studypartner.service.QiniuCloud;
import com.example.studypartner.service.UserService;
import com.example.studypartner.service.impl.QiniuCloudUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;

@SpringBootTest
public class SampleTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Resource
    private OssService ossService;

    @Resource
    private QiniuCloud qiniuCloud;


    //    @Test
//    public void testSelect() {
//        userService.Register("456789","123456","123456");
//    }
    @Test
    public void test() throws IOException {
        QiniuCloudUtil qiniuCloudUtil1 = new QiniuCloudUtil();
        File folder = new File("E:\\picture\\5339.zip");//文件夹路径
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < 1; i++) {
            if (listOfFiles[i].isFile()) {
                qiniuCloud.upload(listOfFiles[i]);
//                FileItem fileItem = createFileItem(listOfFiles[i],listOfFiles[i].getName());
//                CommonsMultipartFile commonsMultipartFile = new CommonsMultipartFile(fileItem);
//                ossService.uploadFileAvatar(commonsMultipartFile);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }

    private FileItem createFileItem(File file, String fieldName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem(fieldName, "text/plain", true, file.getName());
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }
}
