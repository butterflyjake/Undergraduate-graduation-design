package com.campus.aed.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileVO {

    /** 文件访问路径, 如 /upload/xxx.jpg */
    private String url;
}
