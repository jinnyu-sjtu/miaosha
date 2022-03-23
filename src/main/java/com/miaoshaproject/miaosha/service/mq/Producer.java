package com.miaoshaproject.miaosha.service.mq;

import com.miaoshaproject.miaosha.service.model.OrderModel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @Author zxl
 * @Date 2022/2/25 20:33
 * @Version 1.0
 */
@Service
public class Producer {
    /**
     * 引入之后就可以操作RabbitMq了，这里要介绍一个类RabbitTemplate,
     * 顾名思义，这个类的作用和JdbcTemplate、RedisTemplate作用大致相同，
     * 只不过操作的是RabbitMq。接下来我们就要去使用它。
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //@Scheduled注解可以控制方法定时执行
    //fixedDelay控制方法执行的间隔时间，是以上一次方法执行完开始算起，如上一次方法执行阻塞住了，那么直到上一次执行完，并间隔给定的时间后，执行下一次。

    @Scheduled(fixedDelay = 1000L)
    public void finishStockAndOrder(OrderModel orderModel){
        rabbitTemplate.convertAndSend("order", orderModel);
    }
/**
 * rabbitTemplate的convertAndSend()方法可以给指定队列发送消息，
 * 函数有三个参数，第一个是**交换机(exchange)的名字,
 * 第二个是路由键(routing-key)**的名字，
 * 第三个则为消息的内容。
 */


}
