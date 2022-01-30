package com.xxxx.seckilldemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckilldemo.exception.GlobalException;
import com.xxxx.seckilldemo.mapper.UserMapper;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.service.IUserService;
import com.xxxx.seckilldemo.utils.CookieUtil;
import com.xxxx.seckilldemo.utils.MD5Util;
import com.xxxx.seckilldemo.utils.UUIDUtil;
import com.xxxx.seckilldemo.utils.ValidatorUtil;
import com.xxxx.seckilldemo.vo.LoginVo;
import com.xxxx.seckilldemo.vo.RespBean;
import com.xxxx.seckilldemo.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    public RespBean doLogin(HttpServletRequest request, HttpServletResponse response,LoginVo loginVo){
        String mobile=loginVo.getMobile();
        String password = loginVo.getPassword();
//        //判断不能为空
//        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password))
//        {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        //判断手机号码
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //根据手机号码获取用户信息，数据库连接
        User user = userMapper.selectById(mobile);
        if(user==null){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //判断密码是否正确
        if(!MD5Util.fromPassToDBPass(password,user.getSalt()).equals(user.getPassword())){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //生成cookie
        String ticket = UUIDUtil.uuid();
        //存cookie到session
//        request.getSession().setAttribute(ticket,user);
        //将用户信息存入redis中
        redisTemplate.opsForValue().set("user:"+ticket,user);
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if(user!=null){
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }
        return user;
    }

    //更新密码
    @Override
    public RespBean updatePassword(String userTicket, String password,HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket, request, response);
        if(user==null){
            throw new GlobalException(RespBeanEnum.MOBIEL_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDBPass(password,user.getSalt()));
        int result = userMapper.updateById(user);
        if(1==result){
            //删除redis
            redisTemplate.delete("user:"+userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
