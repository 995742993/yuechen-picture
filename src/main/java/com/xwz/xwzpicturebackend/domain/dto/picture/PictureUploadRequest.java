package com.xwz.xwzpicturebackend.domain.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 度星希
 * @createTime 2025/3/25 20:28
 * @description 图片上传请求体
 */

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

}
