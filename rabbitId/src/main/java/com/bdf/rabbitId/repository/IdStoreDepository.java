package com.bdf.rabbitId.repository;

import com.alibaba.fastjson.JSON;
import com.bdf.rabbitId.cache.RedisClient;
import com.bdf.rabbitId.model.IdStore;
import com.bdf.rabbitId.utils.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * author: 田培融  TODO 此处可以修改为接口，多存储实现
 */
@RequiredArgsConstructor(staticName = "with")
public class IdStoreDepository {

    private final static String CACHE_ID_STORE = "id:store";
    @Setter
    @NonNull
    private RedisClient redisClient;

    public void save(IdStore store) throws Exception {
        redisClient.executor("set", jedis -> jedis.hset(CACHE_ID_STORE , store.getKey(), JSON.toJSONString(store)));
    }

    public IdStore queryByStoreKey(String key) throws Exception {
        String result = redisClient.executor("get", jedis -> jedis.hget(CACHE_ID_STORE, key));
        return StringUtils.isNotBlank(result) ? JSON.parseObject(result, IdStore.class) : null;
    }

}
