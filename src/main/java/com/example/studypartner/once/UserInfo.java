package com.example.studypartner.once;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * 创建一个读的对象
 */
@Data
public class UserInfo {
    @ExcelProperty("成员编号")
    private String planetCode;
    @ExcelProperty("成员昵称")
    private String username;

}
