package com.xwz.xwzpicturebackend.common;

import lombok.Data;

/**
 * @author 度星希
 * @createTime 2025/3/23 13:49
 * @description 通用分页请求
 */

@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}
