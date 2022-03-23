package com.miaoshaproject.miaosha.service.mq;

import com.miaoshaproject.miaosha.dao.OrderDOMapper;
import com.miaoshaproject.miaosha.dao.SequenceDOMapper;
import com.miaoshaproject.miaosha.dataobject.OrderDO;
import com.miaoshaproject.miaosha.dataobject.SequenceDO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.model.OrderModel;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author zxl
 * @Date 2022/2/25 20:43
 * @Version 1.0
 */
@Service
@RabbitListener(queues = {"order"})
public class Consumer {

    @Autowired
    private ItemService itemService;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @RabbitHandler
    @Transactional
    public void consume(@Payload OrderModel orderModel) throws BusinessException {
        //落单减库存
        Boolean result = itemService.decreaseStock(orderModel.getItemId(), 1);
        if (!result) {
            throw new BusinessException(EmBusinessError.MYSQL_STOCK_DECREASE_ERROR);
        }

        //生成交易流水号
        orderModel.setId((generatorOrderNo()));
        OrderDO orderDO = convertFromOrderModel(orderModel);

        //订单入库
        orderDOMapper.insertSelective(orderDO);

        return ;
    }

    //生成交易流水号
    private String generatorOrderNo() {
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);

        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();  //当前值
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() +sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6-sequenceStr.length();i++) {
            stringBuilder.append("0");
        }
        stringBuilder.append(sequenceStr);

        //最后2位为分库分表位，此处默认为0
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null){
            return null;
        }

        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
