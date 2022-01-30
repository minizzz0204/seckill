package com.xxxx.seckilldemo.controller;


import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.service.IOrderService;
import com.xxxx.seckilldemo.vo.OrderDetailVo;
import com.xxxx.seckilldemo.vo.RespBean;
import com.xxxx.seckilldemo.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    //订单详情
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user,Long orderId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail=orderService.detail(orderId);
        return RespBean.success(detail);
    }
}
