package com.bdf.rabbitId;


import com.bdf.rabbitId.cache.RedisClient;
import com.bdf.rabbitId.model.IdStore;
import com.bdf.rabbitId.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author YangGuodong
 */

@Slf4j
public class IdGenTest {

    private RedisClient redisClient;

    private BufferAllocator allocator;

    @Before
    public void before() throws Exception {
        RedisConnectionFactory.RedisProperties properties = new RedisConnectionFactory.RedisProperties();
        properties.setHost("192.168.4.25");
        properties.setDatabase(3);
        properties.setPort(6379);
        properties.setPassword("");
        properties.setPrefix("id");
        RedisConnectionFactory factory =  RedisConnectionFactory.with(properties).build();
        redisClient = new RedisClient();
        redisClient.setRedisClientFactory( () -> {
            Jedis jedis = factory.fetchJedisConnector();
            jedis.select(3);
            return jedis;
        });

//        redisClient.executor("del", jedis -> jedis.del("id:store"));
//        redisClient.executor("del", jedis -> jedis.del("msg_id:store"));

        BufferAllocatorTemplate.start(redisClient).build(new IdStore().setKey("test_id").setStep(1000).setFactor(30).setWasteQuota(10));
        BufferAllocatorTemplate.start(redisClient).build(new IdStore().setKey("test_1").setStep(1000).setFactor(30).setWasteQuota(10));

    }

    @Test
    public void testGetId(){
        allocator = BufferAllocatorTemplate.getAllocator("test_id");
        Result res = allocator.get();
        if(res.isSuccess()) {
            System.out.println(res.getId());
        }
    }

    @Test
    public void concurrentTest(){
        long startTime = System.currentTimeMillis();
        int threads  = 100;
        int counts = 1000;
        CountDownLatch latch = new CountDownLatch(threads);
        Set hashSet = new ConcurrentHashSet();
        for(int i = 0; i< threads; i++){
            new Thread(()->{
                int index = 0;
                allocator = BufferAllocatorTemplate.getAllocator("test_1");
                while (index++ < counts){
                    try {
                        Result id = allocator.get();
//                        redisClient.executor("zadd",
//                                jedis -> jedis.zadd("msg_id:store3", Double.valueOf(id.getId()), String.valueOf(id.getId())));
                        hashSet.add(id.getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                latch.countDown();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        try {
            latch.await();
            System.out.println("用时" + (System.currentTimeMillis() - startTime));
            System.out.println("结束了，共执行了" + threads * counts + "次，生成了 " + hashSet.size() + "个ID");
            System.out.println(hashSet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
