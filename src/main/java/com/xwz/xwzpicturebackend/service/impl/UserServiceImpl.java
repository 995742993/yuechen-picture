package com.xwz.xwzpicturebackend.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xwz.xwzpicturebackend.constant.CacheKeyConstant;
import com.xwz.xwzpicturebackend.constant.UserConstant;
import com.xwz.xwzpicturebackend.domain.dto.user.UserQueryRequest;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.domain.enums.UserRoleEnum;
import com.xwz.xwzpicturebackend.domain.vo.user.LoginUserVO;
import com.xwz.xwzpicturebackend.domain.vo.user.UserVO;
import com.xwz.xwzpicturebackend.exception.BusinessException;
import com.xwz.xwzpicturebackend.exception.ErrorCode;
import com.xwz.xwzpicturebackend.exception.ThrowUtils;
import com.xwz.xwzpicturebackend.manager.auth.StpKit;
import com.xwz.xwzpicturebackend.manager.message.EmailManager;
import com.xwz.xwzpicturebackend.manager.redis.RedisCache;
import com.xwz.xwzpicturebackend.mapper.UserMapper;
import com.xwz.xwzpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xwz.xwzpicturebackend.constant.CacheKeyConstant.EMAIL_CODE;
import static com.xwz.xwzpicturebackend.constant.CacheKeyConstant.EMAIL_LOCK;
import static com.xwz.xwzpicturebackend.constant.UserConstant.USER_LOGIN_STATE;
import static com.xwz.xwzpicturebackend.exception.ErrorCode.OPERATION_ERROR;

/**
* @author yuelu
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-03-23 17:24:20
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Resource
    private RedisCache redisCache;

    @Resource
    private EmailManager emailManager;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }


    @Override
    public long userRegisterByEmail(String userAccount, String userPassword, String checkPassword, String codeValue) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword, codeValue)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 检查验证码是否和redis的一致，邮箱即用户名
        String codeKey = EMAIL_CODE + userAccount;
        String code = redisCache.get(codeKey);
        if (code == null || !code.equals(codeValue)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserEmail(userAccount);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        redisTemplate.opsForValue().set(codeKey, code, CODE_TTL, TimeUnit.SECONDS);
        // 邮件发送完成了，并且redis的验证码key也设置好了，然后可以释放掉锁key了，之后如果还是有同样的请求进来会被第二个锁检测到
        redisCache.delete(codeKey);
        String lockKey = EMAIL_LOCK + userAccount;
        redisCache.delete(lockKey);
        return user.getId();
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }



    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }


    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "xwz";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }



    // 统一有效期（秒）5分钟
    private static final long CODE_TTL = 300;


    /**
     * 发送邮箱验证码
     *
     * @param userEmail 用户邮箱
     * @return 验证码 key
     */
    @Override
    @Deprecated
    public String sendEmailCode(String userEmail) {
        // 查询数据库是否已经包含这个邮箱
        Long count = this.getBaseMapper().selectCount(new QueryWrapper<User>().eq("user_email", userEmail));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在, 请直接登录!");
        // 发送验证码
        String code = RandomUtil.randomNumbers(4);
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("code", code);
        // 生成一个唯一 ID, 后面注册前端需要带过来
        String key = UUID.randomUUID().toString();
        // Start >>> 优化内容：异步邮件发送
        CompletableFuture.runAsync(() -> {
            try {
                // 发送验证码
                emailManager.sendEmail(userEmail, "注册验证码 - 月辰云图库", contentMap);
            } catch (Exception e) {
                throw new BusinessException(OPERATION_ERROR, "邮件发送失败");
            }
        }).thenRunAsync(() -> {
            // 当发送验证码错误的时候，不会执行存储验证码到redis
            // 存储验证码到redis 5 分钟过期
            redisCache.set(String.format(CacheKeyConstant.EMAIL_CODE_KEY, key, userEmail), code, 5, TimeUnit.MINUTES);
        });
        // End >>> 优化内容：异步邮件发送
        return key;
	}

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    @Qualifier("releaseLockScript")
    private RedisScript<Long> releaseLockScript;

    /**
     * 安全发送验证码（100%防并发）
     */
    @Override
    public String secureSendCode(String userEmail) {
        String lockKey = EMAIL_LOCK + userEmail;
        String codeKey = EMAIL_CODE + userEmail;
        // 1. 获取分布式锁
        String lockValue = UUID.randomUUID().toString();
        boolean locked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, CODE_TTL, TimeUnit.SECONDS));
        if (!locked) {
            throw new BusinessException(OPERATION_ERROR, "操作过于频繁");
        }
        try {
            // 2. 双重检查是否已存在有效验证码
            if (Boolean.TRUE.equals(redisTemplate.hasKey(codeKey))) {
                throw new BusinessException(OPERATION_ERROR, "验证码已发送");
            }
            // 查询数据库是否已经包含这个邮箱
            Long count = this.getBaseMapper().selectCount(new QueryWrapper<User>().eq("userAccount", userEmail));
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在, 请直接登录!");
            // 3. 生成并存储验证码
            String code = RandomUtil.randomNumbers(4);
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("code", code);
            // Start >>> 优化内容：异步邮件发送
            CompletableFuture.runAsync(() -> {
                try {
                    // 发送验证码
                    emailManager.sendEmail(userEmail, "注册验证码 - 月辰云图库", contentMap);
                } catch (Exception e) {
                    // 发送失败就释放锁
                    redisTemplate.execute(releaseLockScript, Collections.singletonList(lockKey), lockValue);
                    throw new BusinessException(OPERATION_ERROR, "邮件发送失败");
                }
            }).thenRunAsync(() -> {
                // 当发送验证码错误的时候，不会执行存储验证码到redis
                // 存储验证码到redis 5 分钟过期
                // 验证码key需要登完成注册流程之后才释放
                redisTemplate.opsForValue().set(codeKey, code, CODE_TTL, TimeUnit.SECONDS);
                // 邮件发送完成了，并且redis的验证码key也设置好了，然后可以释放掉锁key了，之后如果还是有同样的请求进来会被第二个锁检测到
                redisTemplate.execute(releaseLockScript, Collections.singletonList(lockKey), lockValue);

            });
            return code;
        }
        finally {
            // 不能在此处释放锁，因为在并发场景下提前释放掉锁之后会导致一开始的请求重复打到发邮件那一步
            // 直接在redis客户端执行lua脚本命令
            // redisTemplate.execute(releaseLockScript, Collections.singletonList(lockKey), lockValue);
        }
    }
}




