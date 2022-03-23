package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dataobject.OrderDO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.mq.Producer;
import com.miaoshaproject.miaosha.service.Constants;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.OrderService;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.service.model.OrderModel;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zxl
 * @Date 2022/2/22 10:44
 * @Version 1.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    //售罄商品列表
    private static ConcurrentHashMap<Integer, Boolean> itemSoldOut = new ConcurrentHashMap<>();

    //管理redis数据
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @PostConstruct   //在初始化之前执行
    public void initRedis() {
        //将数据库的商品信息同步到redis中
        List<ItemModel> allItem = itemService.listItem();
        for (ItemModel item : allItem) {
            //存入永久数据
            stringRedisTemplate.opsForValue().set(Constants.ITEM_STOCK_PREFIX+item.getId().toString(), item.getStock() + "");
        }
    }

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        /**
         * 第3版：增加售罄列表
         */
        //商品是否售罄
        if (itemSoldOut.get(itemId) != null) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //1、校验下单状态，下单商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户不存在");
        }
        if (amount <=0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量信息不正确");
        }
        //校验活动信息
        if (promoId != null) {
            //校验对应活动是否存在这个适用商品
            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            } else if (itemModel.getPromoModel().getStatus().intValue() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
            }
        }

        OrderModel orderModel= new OrderModel();

        /**
         * 第2版：通过redis判断库存，并作减库存的操作
         */
        //redis校验库存
        try{
            //库存-1
            Long stock = stringRedisTemplate.opsForValue().decrement(Constants.ITEM_STOCK_PREFIX + itemId.toString());
            //打印出库存
            System.out.println(stock);
            //若库存不足
            if (stock == 0) itemSoldOut.put(itemId, true);
            else if (stock < 0) {
                //将售罄商品加入售罄列表
                itemSoldOut.put(itemId, true);
                //redis的数据加回去
                stringRedisTemplate.opsForValue().increment(Constants.ITEM_STOCK_PREFIX + itemId.toString());
                throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
            }
            /*库存充足
            2、落单减库存！   支付减库存
            Boolean result = itemService.decreaseStock(itemId, amount);
            if (!result) {
                throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
            }*/

            //聚合订单模型
            orderModel.setUserId(userId);
            orderModel.setItemId(itemId);
            orderModel.setAmount(amount);
            if (promoId != null) {
                orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
            } else {
                orderModel.setItemPrice(itemModel.getPrice());
            }
            orderModel.setPromoId(promoId);
            orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

            /**
             * 第4版：异步减库存、生成订单号、订单入库
             */
            producer.finishStockAndOrder(orderModel);

        } catch (Exception e) {
            //如果数据库减库存失败了，redis的库存需要加回去；加回去可能就涉及到售罄列表要更新。
            //此处不做处理
            //stringRedisTemplate.opsForValue().increment(Constants.ITEM_STOCK_PREFIX + itemId.toString());
            //e.printStackTrace();
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        return orderModel;

        /**
         * 第1版：直接访问Mysql判断库存
         */
        //2、落单减库存！   支付减库存
//        Boolean result = itemService.decreaseStock(itemId, amount);
//        if (!result) {
//            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
//        }

        //3、订单入库
//        OrderModel orderModel= new OrderModel();
//        orderModel.setUserId(userId);
//        orderModel.setItemId(itemId);
//        orderModel.setAmount(amount);
//        if (promoId != null) {
//            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
//        } else {
//            orderModel.setItemPrice(itemModel.getPrice());
//        }
//        orderModel.setPromoId(promoId);
//        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

//        //生成交易流水号
//
//        orderModel.setId((generatorOrderNo()));
//
//        OrderDO orderDO = convertFromOrderModel(orderModel);
//        orderDOMapper.insertSelective(orderDO);

        //加上商品的销量
        //itemService.increaseSales(itemId,amount);
        //4、返回前端
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

    //@Transactional(propagation = Propagation.REQUIRES_NEW)
//    private String generatorOrderNo() {
//        //订单号有16位
//        StringBuilder stringBuilder = new StringBuilder();
//        //前8位时间信息，年月日
//        LocalDateTime now = LocalDateTime.now();
//        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
//        stringBuilder.append(nowDate);
//
//        //中间6位为自增序列
//        //获取当前sequence
//        int sequence = 0;
//        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
//        sequence = sequenceDO.getCurrentValue();  //当前值
//        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() +sequenceDO.getStep());
//        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
//
//        String sequenceStr = String.valueOf(sequence);
//        for (int i = 0; i < 6-sequenceStr.length();i++) {
//            stringBuilder.append("0");
//        }
//        stringBuilder.append(sequenceStr);
//
//        //最后2位为分库分表位
//        stringBuilder.append("00");
//
//        return stringBuilder.toString();
//    }
}
