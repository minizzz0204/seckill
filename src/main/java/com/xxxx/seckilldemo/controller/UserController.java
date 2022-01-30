package com.xxxx.seckilldemo.controller;


import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.rabbitmq.MQSender;
import com.xxxx.seckilldemo.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    //用户信息，专门用来测试的
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);

    }

//    //测试发送RabbitMQ消息
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("Hello");
//    }
//
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void mq01(){
//        mqSender.send("Hello");
//    }
//
//    @RequestMapping("/mq/direct01")
//    @ResponseBody
//    public void mq02(){
//        mqSender.send01("Hello red");
//    }
//
//    @RequestMapping("/mq/direct02")
//    @ResponseBody
//    public void mq03(){
//        mqSender.send02("Hello green");
//    }
}
