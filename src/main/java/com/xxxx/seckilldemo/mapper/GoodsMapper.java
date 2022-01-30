package com.xxxx.seckilldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.seckilldemo.pojo.Goods;
import com.xxxx.seckilldemo.vo.GoodsVo;

import java.util.List;


public interface GoodsMapper extends BaseMapper<Goods> {

    //获取商品列表
    List<GoodsVo> findGoodsVo();

    //获取商品详情
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
