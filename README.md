基于springboot平台构建商品秒杀系统并优化并发性能
1. 通过分层模型设计方式完成了用户注册、登陆、查看商品列表、进入商品详情以及倒计时秒杀开始后下单购买的基本流程。
2. 根据商品和用户 id 随机生成地址接口，避免恶意刷单。
3. 使用 redis 缓存商品的数据，在 redis 中进行减库存操作。
4. 使用 ConcurrentHashMap 建立商品售罄表，拦截对 redis 的访问。
5. 使用 RabbitMQ 加快请求响应，异步执行减库存、生成订单流水号、订单入库。

使用 jMeter 压测，2000 条线程循环 5 次抢购，90%Line 从 1s 下降到 300ms。

这是一个自学练手的小项目。