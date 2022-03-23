package com.miaoshaproject.miaosha;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author zxl
 * @Date 2022/2/25 21:07
 * @Version 1.0
 */
@Configuration
public class SpringConfig {

    @Bean
    public Queue orderQueue() {
        return new Queue("order", true);
    }
}
