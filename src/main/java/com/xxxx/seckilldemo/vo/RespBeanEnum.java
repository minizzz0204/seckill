package com.xxxx.seckilldemo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

//公共返回枚举
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"ERROR,服务端异常"),
    //登录模块
    LOGIN_ERROR(500210,"用户名或密码错误"),
    MOBILE_ERROR(500211,"手机号码格式不正确"),
    MOBIEL_NOT_EXIST(500212,"手机号不存在"),
    PASSWORD_UPDATE_FAIL(500213,"更新密码失败"),
    SESSION_ERROR(500215,"用户不存在"),
    //异常处理
    BIND_ERROR(500214,"参数校验异常"),
    //空库存,秒杀模块
    EMPTY_STOCK(500500,"库存不足"),
    //重复秒杀
    REPEAT_ERROR(500501,"该商品没人限购一件"),
    //订单模块
    ORDER_NOT_EXIST(500300,"订单不存在"),
    //请求非法
    REQUEST_ILLEGAL(500502,"请求非法,请重新尝试"),
    ERROR_CAPTCHA(500503,"验证码错误,请重新尝试"),
    ACCESS_LIMIT_REACHED(500504,"超过访问限制，请稍后再试");

    private final Integer code;
    private final String message;

}
