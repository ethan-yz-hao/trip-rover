package org.ethanhao.triprover.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisCache {
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * Cache basic objects, such as Integer, String, entity classes, etc.
     *
     * @param key   Cache key
     * @param value Cache value
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Cache basic objects, such as Integer, String, entity classes, etc.
     *
     * @param key      Cache key
     * @param value    Cache value
     * @param timeout  Timeout
     * @param timeUnit Time unit
     */
    public <T> void setCacheObject(final String key, final T value, final
    Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * Set the expiration time
     *
     * @param key     Redis key
     * @param timeout Timeout
     * @return true=success; false=failure
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * Set the expiration time
     *
     * @param key     Redis key
     * @param timeout Timeout
     * @param unit    Time unit
     * @return true=success; false=failure
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * Get cache basic objects
     *
     * @param key Cache key
     * @return Cache value
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * Delete single object
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * Delete multiple objects
     *
     * @param collection Collection of keys
     * @return
     */
    public long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * Cache List
     *
     * @param key      Cache key
     * @param dataList Cache List
     * @return Cache data object
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * Get cache List
     *
     * @param key Cache key
     * @return Cache data object
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * Cache Set
     *
     * @param key     Cache key
     * @param dataSet Cache Set
     * @return Cache data object
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final
    Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation =
                redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * Get cache Set
     *
     * @param key Cache key
     * @return Cache data object
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Cache Map
     *
     * @param key     Cache key
     * @param dataMap Cache Map
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * Get cache Map
     *
     * @param key Cache key
     * @return Cache data object
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Store data in Hash
     *
     * @param key   Redis key
     * @param hKey  Hash key
     * @param value Hash value
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final
    T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * Get data in Hash
     *
     * @param key  Redis key
     * @param hKey Hash key
     * @return Data object in Hash
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash =
                redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * Delete data in Hash
     *
     * @param key  Redis key
     * @param hkey Hash key
     */
    public void delCacheMapValue(final String key, final String hkey) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hkey);
    }

    /**
     * Get multiple Hash values
     *
     * @param key   Redis key
     * @param hKeys Hash key collection
     * @return Hash value collection
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final
    Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * Get a list of basic objects
     *
     * @param pattern String prefix
     * @return List of basic objects
     */
    public Collection<String> keys(final String pattern) {

        return redisTemplate.keys(pattern);
    }
}
