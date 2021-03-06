package top.inson.springboot.core;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.inson.springboot.lock.IDistributedLocker;
import top.inson.springboot.lock.impl.RedissonDistributedLockerImpl;
import top.inson.springboot.utils.RedissonLockUtil;

@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfiguration {

    private String address;
    private Integer database;
    private String password;
    private Integer timeout;




    @Bean
    RedissonClient redissonClient(){
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer();
        serverConfig.setAddress(address)
                .setTimeout(timeout)
                .setDatabase(database);
        if(StringUtils.isNotEmpty(password))
            serverConfig.setPassword(password);
        return Redisson.create(config);
    }


    @Bean
    IDistributedLocker distributedLocker(RedissonClient redissonClient){
        IDistributedLocker locker = new RedissonDistributedLockerImpl();
        ((RedissonDistributedLockerImpl)locker).setRedissonClient(redissonClient);
        RedissonLockUtil.setRedisLocker(locker);
        return locker;
    }




}
