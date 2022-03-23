package com.miaoshaproject.miaosha.controller;

import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.Constants;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.OrderService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.service.model.OrderModel;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author zxl
 * @Date 2022/2/22 16:36
 * @Version 1.0
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(originPatterns = "*",allowCredentials = "true",allowedHeaders = "*")
public class OrderController extends BaseController{
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 安全优化之 ---接口地址随机化(隐藏)
     * 1.点击秒杀之后，先访问该接口生成一个pathId，并存入redis, 返回前端
     * 2.前端带着这个pathId去访问秒杀接口，如果传入的path和从redis取出的不一致，就认为 非法请求
     */
    //@AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/getPath", method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType getPath(@RequestParam("userId") Integer userId,
                                  @RequestParam(value = "itemId") Integer itemId) throws BusinessException {
        //用户是否登录
        if (stringRedisTemplate.opsForValue().get(Constants.IS_LOGIN_PREFIX + userId) == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        /*随机生成 一个 6 长度的pathId 返回给前端*/
        String pathId = RandomStringUtils.randomAlphanumeric(6);
        //商品和用户  对应一个  pathId   记录在redis中
        stringRedisTemplate.opsForValue().set(Constants.GET_URL_PREFIX +  userId + "&" + itemId, pathId + "");
        return CommonReturnType.create(pathId);
    }

    //封装下单请求
//    @PathVariable("xxx")
//    通过 @PathVariable 可以将URL中占位符参数{xxx}绑定到处理器类的方法形参中@PathVariable(“xxx“)
    @RequestMapping(value = "/{path}/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="userId", required = false)Integer userId,
                                        @RequestParam(name="itemId")Integer itemId,
                                        //@RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId", required = false)Integer promoId,
                                        @PathVariable("path") String path) throws BusinessException {
        if (!stringRedisTemplate.opsForValue().get(Constants.GET_URL_PREFIX + userId + "&" + itemId).equals(path)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "非法访问");
        }
        /**
         * 通过HttpSession校验用户是否登陆1
         */
        /*
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin == null || !isLogin.booleanValue()){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        获取用户的登陆信息
        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        */
        //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        /**
         * 通过redis检测用户是否登录
         * if (stringRedisTemplate.opsForValue().get(Constants.IS_LOGIN_PREFIX + userId) == null) {
         *     throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
         * }
         */

        /**
         * 直接进行下单的压力测试
         */
        OrderModel orderModel = orderService.createOrder(24, itemId, promoId, 1);


        return CommonReturnType.create(null);
    }
}
