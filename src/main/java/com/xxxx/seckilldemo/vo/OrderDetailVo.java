package com.xxxx.seckilldemo.vo;

import com.xxxx.seckilldemo.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderDetailVo {
    private Order order;
    private GoodsVo goodsVo;
}
