package com.xwz.xwzpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 度星希
 * @createTime 2025/3/23 13:49
 * @description 根据ID 删除请求类
 */

@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
