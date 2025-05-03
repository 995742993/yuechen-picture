package com.xwz.xwzpicturebackend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xwz.xwzpicturebackend.domain.dto.spaceuser.SpaceUserAddRequest;
import com.xwz.xwzpicturebackend.domain.dto.spaceuser.SpaceUserQueryRequest;
import com.xwz.xwzpicturebackend.domain.entity.SpaceUser;
import com.xwz.xwzpicturebackend.domain.vo.space.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author yuelu
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-05-02 16:38:13
*/
public interface SpaceUserService extends IService<SpaceUser> {

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
