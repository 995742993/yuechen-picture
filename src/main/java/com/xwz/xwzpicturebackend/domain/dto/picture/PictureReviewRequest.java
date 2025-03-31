package com.xwz.xwzpicturebackend.domain.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 度星希
 * @createTime 2025/3/29 17:46
 * @description 图片审核请求实体
 */

@Data
public class PictureReviewRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 状态：0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;


    private static final long serialVersionUID = 1L;
}
