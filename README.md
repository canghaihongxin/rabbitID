# rabbitID
兔子ID生成器
有以下几个优点
1. id生成是连续的，递增的，对索引比较友好。
2. 使用预申请号段，可以在redis挂掉的情况下使用一段时间。
3. 使用双Segment号段，性能好


# 核心流程讲解

![免子ID生成器获取ID流程](https://img-blog.csdnimg.cn/20200807085318227.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTEyOTYxNjU=,size_16,color_FFFFFF,t_70)

此图加载如果慢的话可以看我的博客 https://blog.csdn.net/u011296165/article/details/107854375


# 性能
在本地电脑跑了跑10万个ID大约是在7秒左右。

# 使用方法

第一步： 添加依赖



`````` <dependency>

		 <dependency>
            <groupId>com.bdf</groupId>
            <artifactId>rabbitId</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>2.9.1</version>
        </dependency>
``````



第二步： 添加配置



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

