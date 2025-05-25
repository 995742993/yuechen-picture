package com.xwz.xwzpicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xwz.xwzpicturebackend.annotation.AuthCheck;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateLimiter;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateLimiters;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateRule;
import com.xwz.xwzpicturebackend.api.aliyunai.AliYunAiApi;
import com.xwz.xwzpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.xwz.xwzpicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.xwz.xwzpicturebackend.api.imagesearch.ImageSearchApiFacade;
import com.xwz.xwzpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.xwz.xwzpicturebackend.common.BaseResponse;
import com.xwz.xwzpicturebackend.common.DeleteRequest;
import com.xwz.xwzpicturebackend.common.ResultUtils;
import com.xwz.xwzpicturebackend.constant.UserConstant;
import com.xwz.xwzpicturebackend.domain.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureEditRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureQueryRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureReviewRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureUpdateRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureUploadByBatchRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.PictureUploadRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.SearchPictureByColorRequest;
import com.xwz.xwzpicturebackend.domain.dto.picture.SearchPictureByPictureRequest;
import com.xwz.xwzpicturebackend.domain.entity.Picture;
import com.xwz.xwzpicturebackend.domain.entity.PictureTagCategory;
import com.xwz.xwzpicturebackend.domain.entity.Space;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.domain.enums.LimitTypeEnum;
import com.xwz.xwzpicturebackend.domain.enums.PictureReviewStatusEnum;
import com.xwz.xwzpicturebackend.domain.vo.picture.PictureVO;
import com.xwz.xwzpicturebackend.exception.BusinessException;
import com.xwz.xwzpicturebackend.exception.ErrorCode;
import com.xwz.xwzpicturebackend.exception.ThrowUtils;
import com.xwz.xwzpicturebackend.manager.auth.SpaceUserAuthManager;
import com.xwz.xwzpicturebackend.manager.auth.StpKit;
import com.xwz.xwzpicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.xwz.xwzpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.xwz.xwzpicturebackend.service.PictureService;
import com.xwz.xwzpicturebackend.service.SpaceService;
import com.xwz.xwzpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.xwz.xwzpicturebackend.constant.UserConstant.DEFAULT_ROLE;

/**
 * @author 度星希
 * @createTime 2025/3/23 17:51
 * @description 图片服务控制层
 */

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    // 注入UserService
    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private AliYunAiApi aliYunAiApi;



    // 初始化本地缓存
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();


    // region 图片上传
    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
    // endregion 图片上传

    // region 图片查看
    /**
     * 根据 id 获取图片（封装类）
     *
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间的图片，需要校验权限
        Space space = null;
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 获取权限列表
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        pictureVO.setPermissionList(permissionList);
        // 获取封装类
        return ResultUtils.success(pictureVO);
    }




    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 编程式鉴权方法，hasPermission方法最终会调用getPermissionList方法，并且这个方法就是我们实现的StpInterface接口的方法
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);

            // 私有空间 - SaToken鉴权替代
//            User loginUser = userService.getLoginUser(request);
//            Space space = spaceService.getById(spaceId);
//            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
//            if (!loginUser.getId().equals(space.getUserId())) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
//            }
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }
    // endregion 图片查看

    // region 图片编辑与删除
    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.editPicture(pictureEditRequest, userService.getLoginUser(request));
        return ResultUtils.success(true);
    }

    /**
     * 删除图片
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    // endregion 图片查看

    // region 其他接口
    /**
     * 相当于字典接口
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 上传的图片审核接口
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 以图搜图：单接口单IP一分钟10次，单接口单用户
     */
    @RateLimiters(rateLimiters = {
            @RateLimiter(
                    limitTypeEnum = LimitTypeEnum.USER_ID,
                    rateRules = {@RateRule(
                            timeDuration = 1,
                            timeUnit = TimeUnit.MINUTES
                    )
                    }),
            @RateLimiter(
                    limitTypeEnum = LimitTypeEnum.IP,
                    rateRules = {@RateRule(
                            timeDuration = 1,
                            timeUnit = TimeUnit.MINUTES
                    )
                    })
    })
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        String url = oldPicture.getUrl() + "?imageMogr2/format/png";
        //List resultList ImageSearchApiFacade.searchlmage(url);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(url);
        return ResultUtils.success(resultList);
    }

    /**
     * 按照颜色搜索
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 创建 AI 扩图任务
     * 完成： 添加接口限流，普通用户每日最多调用5次该接口，单IP也最多5次
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    @RateLimiters(rateLimiters = {
            @RateLimiter(
                    limitTypeEnum = LimitTypeEnum.USER_ID,
                    rateRules = {@RateRule(
                            limit = 5,
                            timeDuration = 24,
                            timeUnit = TimeUnit.HOURS
                    )
                    }),
            @RateLimiter(
                    limitTypeEnum = LimitTypeEnum.IP,
                    rateRules = {@RateRule(
                            limit = 5,
                            timeDuration = 24,
                            timeUnit = TimeUnit.HOURS
                    )
                    })
    })
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (Objects.equals(user.getUserRole(), DEFAULT_ROLE)) {
            // 按用户ID和日期生成Key
            String key = "AI-TASK-LIMIT:" + user.getId() + ":" + LocalDate.now();
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count == 1) {
                // 设置过期时间为当天剩余秒数
                long secondsUntilMidnight = LocalDateTime.now().until(
                        LocalDate.now().plusDays(1).atStartOfDay(),
                        ChronoUnit.SECONDS
                );
                stringRedisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
            }
            ThrowUtils.throwIf(count > 3, ErrorCode.FORBIDDEN_ERROR, "今日调用次数已达上限");
        }
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }



    // endregion 其他接口

    // region 图片查询-缓存（已废弃）
    /**
     * 图片List查询-Redis缓存方案
     * @param pictureQueryRequest
     * @param request
     * @return
     *
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存 key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = "yupicture:listPictureVOByPage:" + hashKey;
        // 从 Redis 缓存中查询
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String cachedValue = valueOps.get(redisKey);
        if (cachedValue != null) {
            // 如果缓存命中，返回结果
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        // 存入 Redis 缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 300 +  RandomUtil.randomInt(0, 300);
        valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        // 返回结果
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 图片List查询-本地缓存方案
     * 分页获取图片列表（封装类，有缓存）
     * 暂时废弃，原因是考虑到私有空间的图片更新频率不好把握，之前编写的缓存分页查询图片接口可以暂不使用和修改
     * @Deprecated
     * @param pictureQueryRequest
     */
    @Deprecated
    @PostMapping("/list/page/vo/byLocalCache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithLocalCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存 key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "listPictureVOByPage:" + hashKey;
        // 从本地缓存中查询
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            // 如果缓存命中，返回结果
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 存入本地缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 返回结果
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 图片List查询-多级缓存方案
     * @param pictureQueryRequest
     * @param request
     * @return
     *
     */
    @Deprecated
    @PostMapping("/list/page/vo/byMultiLevelCache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithMultiLevelCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                           HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存 key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "yupicture:listPictureVOByPage:" + hashKey;

        // 1. 查询本地缓存（Caffeine）
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 2. 查询分布式缓存（Redis）
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null) {
            // 如果命中 Redis，存入本地缓存并返回
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 4. 更新缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 更新本地缓存
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 更新 Redis 缓存，设置过期时间为 5 分钟
        valueOps.set(cacheKey, cacheValue, 5, TimeUnit.MINUTES);
        // 返回结果
        return ResultUtils.success(pictureVOPage);
    }

    // endregion 图片查询-缓存（已废弃）


    // region 仅管理员可用接口

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     *  批量获取图片
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @RateLimiters(rateLimiters = {
            @RateLimiter(
                    limitTypeEnum = LimitTypeEnum.USER_ID,
                    rateRules = {@RateRule(
                            limit = 5,
                            timeDuration = 24,
                            timeUnit = TimeUnit.HOURS
                    )
                    }),
            @RateLimiter(
                    limitTypeEnum = LimitTypeEnum.IP,
                    rateRules = {@RateRule(
                            limit = 5,
                            timeDuration = 24,
                            timeUnit = TimeUnit.HOURS
                    )
                    })
    })
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


    // endregion 仅管理员可用接口





}
