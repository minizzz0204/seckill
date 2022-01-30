package com.xxxx.seckilldemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.vo.LoginVo;
import com.xxxx.seckilldemo.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface IUserService extends IService<User> {

//    RespBean doLogin(LoginVo loginVo);

    RespBean doLogin(HttpServletRequest request, HttpServletResponse response,LoginVo loginVo);
    //根据Cookie获取用户
    User getUserByCookie(String userTicket,HttpServletRequest request,HttpServletResponse response);
    //更新密码
    RespBean updatePassword(String userTicket,String password,HttpServletRequest request, HttpServletResponse response);
}
