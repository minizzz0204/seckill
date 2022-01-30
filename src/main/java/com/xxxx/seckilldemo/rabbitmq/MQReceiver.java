package com.xxxx.seckilldemo.rabbitmq;

import com.xxxx.seckilldemo.pojo.SeckillMessage;
import com.xxxx.seckilldemo.pojo.SeckillOrder;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.service.IGoodsService;
import com.xxxx.seckilldemo.service.IOrderService;
import com.xxxx.seckilldemo.utils.JsonUtil;
import com.xxxx.seckilldemo.vo.GoodsVo;
import com.xxxx.seckilldemo.vo.RespBean;
import com.xxxx.seckilldemo.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.swing.*;

@Service
@Slf4j
public class MQReceiver {

//    @RabbitListener(queues="queue")
//    public void receive(Object msg){
//
//        log.info("接收消息:"+msg);
//    }
//
//    @RabbitListener(queues="queue_fanout01")
//    public void receive01(Object msg){
//        log.info("QUEUE01消息："+msg);
//    }
//
//    @RabbitListener(queues="queue_fanout02")
//    public void receive02(Object msg){
//        log.info("QUEUE02消息："+msg);
//    }
//
//    @RabbitListener(queues="queue_direct01")
//    public void receive03(Object msg){
//        log.info("QUEUE01消息："+msg);
//    }
//
//    @RabbitListener(queues="queue_direct02")
//    public void receive04(Object msg){
//        log.info("QUEUE02消息："+msg);
//    }
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;
    //下单操作
    @RabbitListener(queues="SeckillQueue")
    public void receive(String message){
        log.info("接受到的消息："+message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user=seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if(goodsVo.getStockCount()<1){
            return;
        }
        //判断秒杀订单是否重复抢购,一个用户只能秒杀一个商品
        SeckillOrder seckillOrder =
                (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":"+goodsId);
        if(seckillOrder!=null){
            return;
        }
        //下单操作
        orderService.seckill(user,goodsVo);
    }
}
