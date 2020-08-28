package com.bdf.rabbitId.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

/**
 * @Description: ${todo}(用一句话描述该文件做什么)
 * @Version: 1.0
 * @author: Gordon
 * @date: 2019-08-08
 */
public class RedisConnectionFactory {
    private JedisShardInfo shardInfo;
    private int timeout = Protocol.DEFAULT_TIMEOUT;
    private boolean usePool = true;
    private boolean useSsl = false;
    private Pool<Jedis> pool;
    private final JedisPoolConfig poolConfig;
    private int dbIndex = 0;

    private String clientName;

    private RedisProperties redisProperties;

    public RedisProperties getRedisProperties() {
        return redisProperties;
    }

    public void setRedisProperties(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    public RedisConnectionFactory(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public static RedisConnectionFactory with(RedisProperties redisProperties) {
        JedisPoolConfig poolConfig = null == redisProperties.getPool() ? new JedisPoolConfig() : jedisPoolConfig(redisProperties.getPool());
        RedisConnectionFactory factory = new RedisConnectionFactory(poolConfig);
        factory.setRedisProperties(redisProperties);
        return factory;
    }

    private static JedisPoolConfig jedisPoolConfig(JedisPool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        config.setMaxWaitMillis((long) pool.getMaxWait());
        return config;
    }


    public RedisConnectionFactory build() {

        if (shardInfo == null) {
            shardInfo = new JedisShardInfo(redisProperties.getHost(), redisProperties.getPort());

            if (StringUtils.isNotEmpty(redisProperties.getPassword())) {
                shardInfo.setPassword(redisProperties.getPassword());
            }
            shardInfo.setSoTimeout(redisProperties.getTimeout() > 0 ? redisProperties.getTimeout() : timeout);
        }
        if (usePool) {
            this.pool = createPool();
        }
        this.dbIndex = redisProperties.getDatabase();
        return this;
    }

    private Pool<Jedis> createPool() {
        return new redis.clients.jedis.JedisPool(this.poolConfig, shardInfo.getHost(), shardInfo.getPort(),
                shardInfo.getSoTimeout(), shardInfo.getPassword(), this.dbIndex, clientName, useSsl);
    }

    public void destroy() {
        if (usePool && pool != null) {
            try {
                pool.destroy();
            } catch (Exception ex) {
//                log.warn("Cannot properly close Jedis pool", ex);
            }
            pool = null;
        }
    }

    /**
     * Returns a Jedis instance to be used as a Redis connection. The instance can be newly created or retrieved from a
     * pool.
     *
     * @return Jedis instance ready for wrapping into a {@link }.
     */
    protected Jedis fetchJedisConnector() {
        try {

            if (usePool && pool != null) {
                return pool.getResource();
            }

            Jedis jedis = new Jedis(shardInfo);
            // force initialization (see Jedis issue #82)
            jedis.connect();

            potentiallySetClientName(jedis);
            return jedis;
        } catch (Exception ex) {
            throw new RuntimeException("Cannot get Jedis connection", ex);
        }
    }

    private void potentiallySetClientName(Jedis jedis) {

        if (StringUtils.isNotEmpty(clientName)) {
            jedis.clientSetname(clientName);
        }
    }

    public Jedis getJedisConnection() {
        Jedis jedis = fetchJedisConnector();
        if (dbIndex > 0 && jedis != null) {
            jedis.select(dbIndex);
        }
        return jedis;
    }

    public static class RedisProperties {

        private int database = 0;
        private String url;
        private String host = "localhost";
        private String password;
        private int port = 6379;
        private boolean ssl;
        private int timeout;
        private String prefix;

        private JedisPool pool;

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public JedisPool getPool() {
            return pool;
        }

        public void setPool(JedisPool pool) {
            this.pool = pool;
        }
    }
    public static class JedisPool {
        private int maxIdle = 8;
        private int minIdle = 0;
        private int maxActive = 8;
        private int maxWait = -1;

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(int maxWait) {
            this.maxWait = maxWait;
        }
    }
}
