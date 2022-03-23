package com.miaoshaproject.miaosha;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class MiaoshaApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {

    }

    @Test
    void testRedis() {
        //增
        stringRedisTemplate.opsForValue().set("name","Test");
        String name = (String)stringRedisTemplate.opsForValue().get("name");
        System.out.println(name);
    }

}
