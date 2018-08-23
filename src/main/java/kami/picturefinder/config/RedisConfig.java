package kami.picturefinder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@PropertySource(value = "classpath:init.properties", encoding = "UTF-8")
public class RedisConfig {

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

    //客户端指定时间断开
    @Value("${redis.timeout}")
    private int redisTimeout;

    @Value("${redis.ip}")
    private String defaultRedisIp;

    @Value("${redis.port}")
    private int defaultRedisPort;

    @Value("${redis.password}")
    private String defaultRedisPassword;

    //哨兵模式ip1
    @Value("${redis.sentinel.host1}")
    private String  sentinelHost1;

    //哨兵模式端口1
    @Value("${redis.sentinel.port1}")
    private Integer sentinelPort1;

    //哨兵模式ip2
    @Value("${redis.sentinel.host2}")
    private String  sentinelHost2;

    //哨兵模式端口2
    @Value("${redis.sentinel.port2}")
    private Integer sentinelPort2;

    /**
     * redis连接池配置
     * @return redis连接池配置对象
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig(){
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
        return config;
    }

    /**
     * 哨兵模式配置
     * @return 哨兵配置对象
     */
    private RedisSentinelConfiguration redisSentinelConfiguration(){
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.master("mymaster");
        redisSentinelConfiguration.sentinel(sentinelHost1, sentinelPort1);
        redisSentinelConfiguration.sentinel(sentinelHost2, sentinelPort2);
        /*RedisNode sentinel1 = new RedisNode(sentinelHost1, sentinelPort1);
        redisSentinelConfiguration.addSentinel(sentinel1);*/
        return redisSentinelConfiguration;
    }

    /**
     * Spring Data Redis 连接工厂
     * 这里使用Jedis作为Redis客户端
     * @return jedis连接工厂
     */
    @Bean(destroyMethod = "destroy")
    public JedisConnectionFactory jedisConnectionFactory(){
        //哨兵模式连接
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration(), jedisPoolConfig());
        //正常模式
        //JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(jedisPoolConfig());
        jedisConnectionFactory.setHostName(defaultRedisIp);
        jedisConnectionFactory.setPort(defaultRedisPort);
        jedisConnectionFactory.setPassword(defaultRedisPassword);
        jedisConnectionFactory.setTimeout(redisTimeout);
        return jedisConnectionFactory;
    }

    /**
     * redis模板
     * @return 返回spring封装的redis模板对象
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }
}
