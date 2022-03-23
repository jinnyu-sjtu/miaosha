package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.service.model.OrderModel;
import org.springframework.stereotype.Service;

/**
 * @Author zxl
 * @Date 2022/2/22 10:40
 * @Version 1.0
 */
public interface OrderService {
    //1.通过前端url上传秒杀活动id，然后下单接口校验对应Id是否属于对应商品且活动已经开始
    // （一个商品可能存在多个秒杀活动中；或者意味着随便一个普通商品也要查询活动信息，对性能伤害大）
    //2.直接在下单路径中判断商品的活动信息
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;


}
