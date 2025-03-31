package com.xwz.xwzpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureQueryRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureReviewRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureUploadByBatchRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureUploadRequest;
import com.xwz.xwzpicturebackend.domain.entity.Picture;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.domain.vo.picture.PictureVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author yuelu
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-29 09:46:37
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

}
