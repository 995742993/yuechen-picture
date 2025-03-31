package com.xwz.xwzpicturebackend.domain.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 度星希
 * @createTime 2025/3/29 14:36
 * @description TODO
 */

@Data
public class PictureTagCategory {

    private List<String> tagList;

    private List<String> categoryList;
}
