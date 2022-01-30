package com.xxxx.seckilldemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckilldemo.mapper.SeckillOrderMapper;
import com.xxxx.seckilldemo.pojo.SeckillOrder;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Long getResult(User user, Long goodsId) {
        //获取秒杀订单,返回orderId,若有orderId则成功，-1则是失败，0则是排队中
        SeckillOrder seckillOrder=seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().
                eq("user_id",user.getId()).eq("goods_id",goodsId));
        if(seckillOrder!=null){
            return seckillOrder.getOrderId();
        }else if(redisTemplate.hasKey("isStockEmpty:"+goodsId)){
            return -1L;
        }else{
            return 0L;
        }
    }
}
