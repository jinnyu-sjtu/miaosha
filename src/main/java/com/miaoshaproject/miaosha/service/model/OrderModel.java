package com.miaoshaproject.miaosha.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author zxl
 * @Date 2022/2/22 10:28
 * @Version 1.0
 */

/**
 * Serializable ： 使得OrderModel可以被序列化，可以被放入消息列表中
 * 用户下单交易模型
 */
public class OrderModel implements Serializable {
    //订单号要有明显的属性，是String类型的
    private String id;

    //购买的用户id
    private Integer userId;

    //商品购买的单价，如果promoId非空，则表示秒杀商品价格
    private BigDecimal itemPrice;

    //购买的商品id
    private Integer itemId;

    //若非空，则是以秒杀商品方式下单
    private Integer promoId;

    //购买数量
    private Integer amount;

    //购买金额，如果promoId非空，则表示秒杀商品价格
    private BigDecimal orderPrice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }
}
