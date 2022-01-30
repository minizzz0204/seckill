package com.xxxx.seckilldemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.service.IUserService;
import com.xxxx.seckilldemo.utils.CookieUtil;
import com.xxxx.seckilldemo.vo.RespBean;
import com.xxxx.seckilldemo.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class AccessInterceptor implements HandlerInterceptor{

    @Autowired
    private IUserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler)throws Exception{
        if(handler instanceof HandlerMethod){
            User user=getUser(request,response);//获取当前用户
            UserContext.setUser(user);
            HandlerMethod hm=(HandlerMethod)handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                return true;
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if(needLogin){
                if(user==null){
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
                key+=":"+user.getId();
            }
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count=(Integer)valueOperations.get(key);
            if(count==null){
                valueOperations.set(key,1,second, TimeUnit.SECONDS);
            }else if(count<maxCount){
                valueOperations.increment(key);
            }else{
                render(response,RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    //获取当前登录用户
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if(StringUtils.isEmpty(ticket)){
            return null;
        }
        return userService.getUserByCookie(ticket,request,response);
    }

    private void render(HttpServletResponse response,RespBeanEnum respBeanEnum)throws IOException{
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
        PrintWriter out=response.getWriter();
        RespBean bean= RespBean.error(respBeanEnum);
        out.write(new ObjectMapper().writeValueAsString(bean));
        out.flush();
        out.close();
    }
}

