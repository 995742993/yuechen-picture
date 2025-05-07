package com.xwz.xwzpicturebackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xwz.xwzpicturebackend.domain.dto.analyze.SpaceCategoryAnalyzeRequest;
import com.xwz.xwzpicturebackend.domain.dto.analyze.SpaceRankAnalyzeRequest;
import com.xwz.xwzpicturebackend.domain.dto.analyze.SpaceSizeAnalyzeRequest;
import com.xwz.xwzpicturebackend.domain.dto.analyze.SpaceTagAnalyzeRequest;
import com.xwz.xwzpicturebackend.domain.dto.analyze.SpaceUsageAnalyzeRequest;
import com.xwz.xwzpicturebackend.domain.dto.analyze.SpaceUserAnalyzeRequest;
import com.xwz.xwzpicturebackend.domain.entity.Space;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.domain.vo.analyze.SpaceCategoryAnalyzeResponse;
import com.xwz.xwzpicturebackend.domain.vo.analyze.SpaceSizeAnalyzeResponse;
import com.xwz.xwzpicturebackend.domain.vo.analyze.SpaceTagAnalyzeResponse;
import com.xwz.xwzpicturebackend.domain.vo.analyze.SpaceUsageAnalyzeResponse;
import com.xwz.xwzpicturebackend.domain.vo.analyze.SpaceUserAnalyzeResponse;

import java.util.List;

/**
 * @author yuelu
 * @createDate
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 获取空间使用情况分析
     *
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 获取空间用户上传行为分析
     *
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 空间使用排行分析（仅管理员）
     *
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
