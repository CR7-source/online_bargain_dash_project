package com.miaoshaproject.service.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:CR7-source
 * @Date: 2022/02/07/ 12:30
 * @Description
 */
@Data
//用户下单的交易模型
public class OrderModel {
    //交易单号，例如2019052100001212，使用string类型
    private String id;

    //购买的用户id
    private Integer userId;

    //购买的商品id
    private Integer itemId;

    //购买时商品的单价
    private BigDecimal itemPrice;

    //购买数量
    private Integer amount;

    //购买金额
    private BigDecimal orderPrice;

}
