package kami.picturefinder.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @Description redis工具类
 * @ClassName JedisUtil
 * @author 李英夫
 * @since 2018/8/20 18:04
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
public class JedisUtil {

    @Value("${redis.maxTotal}")
    private int redisMaxTotal;

    @Value("${redis.maxIdle}")
    private int redisMaxIdle;

    @Value("${redis.minIdle}")
    private int redisMinIdle;

    @Value("${redis.maxWaitMillis}")
    private long redisMaxWaitMillis;

    @Value("${redis.testOnBorrow}")
    private boolean redisTestOnBorrow;

    @Value("${redis.testOnReturn}")
    private boolean redisTestOnReturn;

    //重试次数
    @Value("${redis.retry}")
    private int redisRetry;

    @Value("${redis.ip}")
    private String defaultRedisIp;

    @Value("${redis.port}")
    private int defaultRedisPort;

    @Value("${redis.password}")
    private String defaultRedisPassword;

    private static class RedisUtilHolder{
        private static final JedisUtil instance = new JedisUtil();
    }

    private JedisUtil jedisUtil(){
        return null;
    }

    public static JedisUtil getInstance(){
        return RedisUtilHolder.instance;
    }

    private static Map<String, JedisPool> jedisPools = new ConcurrentHashMap<String, JedisPool>();

    private JedisPool getPool(String ip, int port, String redisPassword){
        String key = ip + ":" + port;
        JedisPool pool = null;
        if(!jedisPools.containsKey(key)){
            JedisPoolConfig config = new JedisPoolConfig();
            //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
            //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
            config.setMaxTotal(redisMaxTotal);
            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值为8。
            config.setMaxIdle(redisMaxIdle);
            //设置最小空闲数
            config.setMinIdle(redisMinIdle);
            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；单位毫秒
            //小于零:阻塞不确定的时间,  默认-1
            config.setMaxWaitMillis(redisMaxWaitMillis);
            //在borrow(引入)一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
            config.setTestOnBorrow(redisTestOnBorrow);
            //return 一个jedis实例给pool时，是否检查连接可用性（ping()）
            config.setTestOnReturn(redisTestOnReturn);
            pool = new JedisPool(config, ip, port, 2000, redisPassword);
            jedisPools.put(key, pool);
        }else{
            pool = jedisPools.get(key);
        }
        return pool;
    }

    public Jedis getJedis(){
        return  getJedis(defaultRedisIp, defaultRedisPort, defaultRedisPassword);
    }

    public Jedis getJedis(String ip, int port, String redisPassword){
        Jedis jedis = null;
        int currRetry = redisRetry;
        do {
            try {
                jedis = getPool(ip, port, redisPassword).getResource();
            }catch (Exception e){
                e.printStackTrace();
            }
        }while (jedis == null && currRetry-- > 0);
        if(jedis == null){
            throw new JedisException("Could not get a resource from the pool");
        }
        return jedis;
    }
}
