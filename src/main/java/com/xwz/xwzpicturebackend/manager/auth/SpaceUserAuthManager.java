package com.xwz.xwzpicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xwz.xwzpicturebackend.domain.entity.Space;
import com.xwz.xwzpicturebackend.domain.entity.SpaceUser;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.domain.enums.SpaceRoleEnum;
import com.xwz.xwzpicturebackend.domain.enums.SpaceTypeEnum;
import com.xwz.xwzpicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.xwz.xwzpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.xwz.xwzpicturebackend.manager.auth.model.SpaceUserRole;
import com.xwz.xwzpicturebackend.service.SpaceUserService;
import com.xwz.xwzpicturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xwz.xwzpicturebackend.constant.UserConstant.SUPER_ADMIN_ROLE;

/**
 * 空间成员权限管理
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     *
     * @param spaceUserRole
     * @return
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles()
                .stream()
                .filter(r -> r.getKey().equals(spaceUserRole))
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }


    /**
     * 获取权限列表，当前只运用在了这些地方：
     * 图片查看详情页，因为图片详情页涉及到了写操作按钮，所以直接在返回图片VO的同时就返回当前用户的角色对应的权限列表，
     * 但是也得分场景，当前共有三个场景：公共，私有，团队
     * 公共的话有管理员权限（CRUD）和用户权限（R）
     * 私有的话有管理员（CURD）
     * 团队的话就要分具体的角色了：管理员（图片CURD+成员CRUD），写者（图片CURD），读者（R）
     *
     *
     * @param space
     * @param loginUser
     * @return
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        if (loginUser.getUserRole().equals(SUPER_ADMIN_ROLE)) {
            return ADMIN_PERMISSIONS;
        }
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限。管理员（图片CURD+成员CRUD），写者（图片CURD），读者（R）
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }


}
