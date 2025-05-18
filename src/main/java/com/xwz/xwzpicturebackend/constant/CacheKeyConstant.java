package com.xwz.xwzpicturebackend.constant;

/**
 * @author 度星希
 * @createTime 2025/5/17 16:17
 * @description 缓存 KEY 常量接口
 */
public interface CacheKeyConstant {
	/**
	 * 邮箱验证码缓存 KEY 前缀-废弃方法
	 */
	String EMAIL_CODE_KEY = "EMAIL_CODE_KEY:%s:%s";


	/**
	 * 邮箱验证码缓存 KEY 前缀-并发安全;
	 */
	String EMAIL_CODE = "EMAIL:CODE:";


	/**
	 * 邮箱验证码缓存 KEY 前缀-并发安全;
	 */
	String EMAIL_LOCK = "EMAIL:LOCK:";

	/**
	 * 图形验证码缓存 KEY 前缀
	 */
	String CAPTCHA_CODE_KEY = "CAPTCHA_CODE_KEY:%s";
}
