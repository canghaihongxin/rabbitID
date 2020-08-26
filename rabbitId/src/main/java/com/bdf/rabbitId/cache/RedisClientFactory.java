package com.bdf.rabbitId.cache;

import redis.clients.jedis.Jedis;

/**
 * author: 田培融
 */
public interface RedisClientFactory {
    /**
     *  获取Jedis连接
     * @return Jedis
     */
    Jedis getJedisConnection();
}
