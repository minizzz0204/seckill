package com.xxxx.seckilldemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckilldemo.pojo.Order;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.vo.GoodsVo;
import com.xxxx.seckilldemo.vo.OrderDetailVo;


public interface IOrderService extends IService<Order> {

    //秒杀
    Order seckill(User user, GoodsVo goods);

    //订单详情
    OrderDetailVo detail(Long orderId);

    //获取秒杀地址
    String createPath(User user, Long goodsId);
    //校验秒杀地址
    boolean checkPath(User user, Long goodsId,String path);

    //校验验证码
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
