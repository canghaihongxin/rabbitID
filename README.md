# rabbitID 介绍

兔子ID生成器,名子和RabbitMQ一点关系没有，只是觉得性能还能就起了一个叫免子ID生成器。 现在ID生成器很多。百度的，美团的,还有雪花算法等等等等。这个就是仿的美团的，纯属小菜鸟练手。 

刚开始的时候，觉得好难好难，这样包命名都不能理解。 看源码的时候也看不懂，里面会有一些多线程的东西，觉得用的好巧妙。 后来能看懂，能改一点，要真的感谢我的一个好朋友，教了我很多东西，超级牛。 

看源码有一个捷径就是看设计模式，而且要在开发当中应用一些。不能只是应付面试，还有多线程，也一样。不能只是背背面试题，应付一下面试。当然我自己做的也不多，设计模式和多线程用的也不多。以后还要经常用。当然得有适合的应用场景。



有以下几个优点
1. id生成是连续的，递增的，对索引比较友好。
2. 使用预申请号段，可以在redis挂掉的情况下使用一段时间。
3. 使用双Segment号段，性能好


下一步需要做的：

1. 框架依赖于redis，这一点完全也可抽离出来，可以使用zk，或者mq。  这里面大概又会用到工厂模式和建造者模式，要对持久化进行解耦。  预计今年十一月开始开发。。。。。。


# 性能
在本地电脑跑了跑10万个ID大约是在6秒左右。

# 使用方法

第一步： 添加依赖



``````
        <dependency>
            <groupId>com.budongfeng</groupId>
            <artifactId>rabbitId</artifactId>
            <version>1.0</version>
        </dependency>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>2.9.1</version>
        </dependency>
``````



第二步： 添加配置

注： 这里并不是强依赖于springboot，只是RabbitId在启动程序的时候需要启动构建，如果使用spring的时候也只是需要让应用一启动的时候执行 `run`方法中的代码就可以了。

```java
package com.budongfeng.tboot.config.rabbitid;

import com.bdf.rabbitId.BufferAllocatorTemplate;
import com.bdf.rabbitId.cache.RedisClient;
import com.bdf.rabbitId.model.IdStore;
import com.bdf.rabbitId.utils.RedisConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * description: 配置RabbitID
 * author: 田培融
 * date: 2020-08-28 11:15
 */
@Component
public class RabbitIDConfig implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {


        RedisConnectionFactory.RedisProperties properties = new RedisConnectionFactory.RedisProperties();
        properties.setHost("192.168.4.26");
        properties.setDatabase(3);
        properties.setPort(6379);
        properties.setPassword("");
        properties.setPrefix("id");
        RedisConnectionFactory factory =  RedisConnectionFactory.with(properties).build();

        RedisClient redisClient = new RedisClient();
        redisClient.setRedisClientFactory( () -> {
            Jedis jedis = factory.getJedisConnection();
            jedis.select(3);
            return jedis;
        });
        /**
         * @description:  配置id 
         * @author: 田培融
         * @date: 2020/8/28 14:19
          * @param args	 id 名称  step 申请号段步长   factor 申请号段因子   wasteQuota 损耗额度
         */
        BufferAllocatorTemplate.start(redisClient).build(new IdStore().setKey("user_id").setStep(1000).setFactor(30).setWasteQuota(10));
        BufferAllocatorTemplate.start(redisClient).build(new IdStore().setKey("order_id").setStep(1000).setFactor(30).setWasteQuota(10));
    }
}

```



第三步： 使用



```java
  /**
     * @description:  通过RabbitID 获取id
     * @author: 田培融
     * @date: 2020/8/28 13:06
     */
    @GetMapping("/id")
    @ResponseBody
    public String getId() {
        BufferAllocator allocator = BufferAllocatorTemplate.getAllocator("user_id");
        com.bdf.rabbitId.model.Result res = allocator.get();
        if (res.isSuccess()) {
            System.out.println(res.getId());
        }
        return String.valueOf(res.getId());
    }
```

