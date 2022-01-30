package com.xxxx.seckilldemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckilldemo.pojo.Goods;
import com.xxxx.seckilldemo.vo.GoodsVo;

import java.util.List;


public interface IGoodsService extends IService<Goods> {

    //获取商品列表
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
