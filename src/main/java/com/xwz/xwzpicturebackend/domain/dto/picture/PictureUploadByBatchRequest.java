package com.xwz.xwzpicturebackend.domain.dto.picture;

import lombok.Data;

/**
 * @author 度星希
 * @createTime 2025/3/29 22:21
 * @description TODO
 */

@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;
}
