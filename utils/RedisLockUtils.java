package com.timesbigdata.fullstack.dataplatform.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @auther 阳少文
 * @create 2019-06-21 9:44
 */
@Component
public class RedisLockUtils {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    private final Logger logger = LoggerFactory.getLogger(RedisLockUtils.class);

    public boolean tryLock(String key, String value, Long expire) {
        try {
            RedisCallback<String> callback = (connection) -> {
                JedisCommands commands = (JedisCommands)connection.getNativeConnection();
                return commands.set(key, value, "NX", "PX", expire);
            };

            String result = redisTemplate.execute(callback);
            //不等于null 为true 等于null false
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            logger.error("set redis occured an exception", e);
        }

        return false;
    }

    public void lock(String key, String value, Long expire) {
        try {
            while (!tryLock(key, value, expire)) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            logger.error("set redis occured an exception", e);
        }
    }

    public String get(String key) {
        try {
            RedisCallback<String> callback = (connection) -> {
                JedisCommands commands = (JedisCommands)connection.getNativeConnection();
                return commands.get(key);
            };

            return redisTemplate.execute(callback);
        } catch (Exception e) {
            logger.error("get redis occured an exception", e);
        }

        return null;
    }

    /**
     * <b>Description:</b>
     * <pre>
     *   释放锁的时候，有可能因为持锁之后方法执行时间大于锁的有效期，此时有可能已经被另外一个线程持有锁，所以不能直接删除
     * </pre>
     *
     * @param key
     * @param value
     * @return
     *
     * @since JDK 1.8
     */
    public boolean unLock(String key, String value) {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        List<String> args = new ArrayList<>();
        args.add(value);
        try {
            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            // spring自带的执行脚本方法中，集群模式直接抛出不支持执行脚本的异常，所以只能拿到原redis的connection来执行脚本
            RedisCallback<Long> callback = (connection) -> {
                Object nativeConnection = connection.getNativeConnection();
                // 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                if (nativeConnection instanceof JedisCluster) {// 集群模式
                    return (Long)((JedisCluster)nativeConnection).eval(UNLOCK_LUA, keys, args);
                } else if (nativeConnection instanceof Jedis) {// 单机模式
                    return (Long)((Jedis)nativeConnection).eval(UNLOCK_LUA, keys, args);
                } else {
                    // 模式匹配不上
                }

                return 0L;
            };

            Long result = redisTemplate.execute(callback);

            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("release lock occured an exception", e);
        } finally {
            // 清除掉ThreadLocal中的数据，避免内存溢出
            // lockFlag.remove();
        }

        return false;
    }


}
