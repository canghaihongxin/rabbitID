package com.bdf.rabbitId.cache;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.function.Function;

/**
 * author: 田培融
 */
@Slf4j
public class RedisClient {
    private final String LOG_FLAG = "Redis.%s";
    @Setter
    private RedisClientFactory redisClientFactory;

    private static final String _SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    private static final Long RELEASE_SUCCESS = 1L;

    public boolean distributedLock(String key,String value, int expire) throws Exception {
        String logFlag = LOG_FLAG + "distributedLock";
        String res = executor(logFlag, jedis -> jedis.set(key, value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expire));
        return _SUCCESS.equals(res);
    }

    public boolean releaseDistributedLock(String key,String value) throws Exception {
        String logFlag = LOG_FLAG + "distributedLock";
        String script = "if redis.call('get', KEYS[1]) == KEYS[2] then return redis.call('del', KEYS[1]) else return 0 end";
        Object res = executor(logFlag, jedis -> jedis.eval(script,2,key, value));
        return RELEASE_SUCCESS.equals(res);
    }

    public <T> T executor(String opt, Function<Jedis, T> exec) throws Exception {
        Jedis jedis = null;
        try {
            jedis = redisClientFactory.getJedisConnection();
            return exec.apply(jedis);
        } catch (Exception e) {
            log.error("[{}] - error:", String.format(LOG_FLAG, opt), e);
            throw new Exception(e);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
}
