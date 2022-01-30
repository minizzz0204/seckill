package com.xxxx.seckilldemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckilldemo.pojo.SeckillOrder;
import com.xxxx.seckilldemo.pojo.User;


public interface ISeckillOrderService extends IService<SeckillOrder> {

    //获取秒杀结果
    Long getResult(User user, Long goodsId);
}
