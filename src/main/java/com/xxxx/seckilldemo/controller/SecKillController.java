package com.xxxx.seckilldemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.xxxx.seckilldemo.config.AccessLimit;
import com.xxxx.seckilldemo.exception.GlobalException;
import com.xxxx.seckilldemo.pojo.Order;
import com.xxxx.seckilldemo.pojo.SeckillMessage;
import com.xxxx.seckilldemo.pojo.SeckillOrder;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.rabbitmq.MQSender;
import com.xxxx.seckilldemo.service.IGoodsService;
import com.xxxx.seckilldemo.service.IOrderService;
import com.xxxx.seckilldemo.service.ISeckillOrderService;
import com.xxxx.seckilldemo.utils.JsonUtil;
import com.xxxx.seckilldemo.vo.GoodsVo;
import com.xxxx.seckilldemo.vo.RespBean;
import com.xxxx.seckilldemo.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//秒杀
@Controller
@Slf4j
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

//    @Autowired
//    private RedisScript<Long> script;

    private Map<Long,Boolean> EmptyStockMap=new HashMap<>();

    @RequestMapping("/doSeckill2")
    public String doSeckill2(Model model, User user,Long goodsId) {
        if(user==null){
            return "login";
        }
        model.addAttribute("user",user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if(goods.getStockCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断秒杀订单是否重复抢购,一个用户只能秒杀一个商品
        SeckillOrder seckillOrder =
                seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
                        .eq("goods_id", goodsId));
        if(seckillOrder!=null){
            model.addAttribute("errmsg",RespBeanEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }
        Order order=orderService.seckill(user,goods);
        model.addAttribute("order",order);
        model.addAttribute("goods",goods);
        return "orderDetail";
    }

    @RequestMapping(value="/{path}/doSeckill",method= RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId) {
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations=redisTemplate.opsForValue();
        boolean check=orderService.checkPath(user,goodsId,path);
        if(!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //判断秒杀订单是否重复抢购,一个用户只能秒杀一个商品
        SeckillOrder seckillOrder =
                (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":"+goodsId);
        if(seckillOrder!=null){
            // model.addAttribute("errmsg",RespBeanEnum.REPEAT_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //内存标记，减少Redis的访问
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存操作
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stock<0){
            EmptyStockMap.put(goodsId,true);
            valueOperations.increment("seckillGoods:"+goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //下单
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);

        /*
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if(goods.getStockCount()<1){
           // model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //判断秒杀订单是否重复抢购,一个用户只能秒杀一个商品
//        SeckillOrder seckillOrder =
//                seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
//                        .eq("goods_id", goodsId));
        SeckillOrder seckillOrder =
                (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":"+goodsId);
        if(seckillOrder!=null){
           // model.addAttribute("errmsg",RespBeanEnum.REPEAT_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        Order order=orderService.seckill(user,goods);
//预减库存
 //Long stock = (Long)redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);

        return RespBean.success(order);*/
    }

    //获取秒杀结果，返回orderId,若有orderId则成功，-1则是失败，0则是排队中
    @RequestMapping(value="/result",method =RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId= seckillOrderService.getResult(user,goodsId);
        return RespBean.success(orderId);
    }

    //获取秒杀地址
    @AccessLimit(second=5,maxCount=5,needLogin=true)
    @RequestMapping(value="/path",method=RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
//        ValueOperations valueOperations=redisTemplate.opsForValue();
//        //限制访问次数，5秒内访问5次
//        String uri=request.getRequestURI();
//        //方便测试
//        captcha="0";
//        Integer count=(Integer)valueOperations.get(uri+":"+user.getId());
//        if(count==null){
//            valueOperations.set(uri+":"+user.getId(),1,5,TimeUnit.SECONDS);
//        }else if(count<5){
//            valueOperations.increment(uri+":"+user.getId());
//        }else{
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
//        }

        boolean check=orderService.checkCaptcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str=orderService.createPath(user,goodsId);
        return RespBean.success(str);
    }

    @RequestMapping(value = "/captcha",method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if (user==null||goodsId<0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置图片类型
        response.setContentType("image/jpg");
        response.setHeader("Pragma","No-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setDateHeader("Expires",0);
        //生成验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId, captcha.text(),5, TimeUnit.MINUTES);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败",e.getMessage());
        }
    }

    //系统初始化
    //将商品库存数量加载到Redis中
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(goodsVo ->{
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(),false);
        }
        );
    }


}
