package top.inson.springboot.web;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.inson.springboot.constants.RedisConstants;
import top.inson.springboot.utils.RedisUtil;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Log4j2
@RestController
@RequestMapping(value = "/redis")
public class RedisController {
    @Autowired
    private RedisUtil redisUtil;


    //秒杀抢券场景
    /*此种方式在高并发场景下会出现多发券的情况*/
    @GetMapping("/spike/{productId}")
    public String spike(@PathVariable String productId){
        log.info("productId:{}", productId);

        int stock = Integer.parseInt(redisUtil.get(RedisConstants.GOODS_STOCK_KEY).toString());
        if(stock > 0){
            stock--;
            log.info("剩余stock:{}", stock);
            redisUtil.set(RedisConstants.GOODS_STOCK_KEY, stock);
            return "success";
        }else {
            log.info("已抢购一空stock:{}", stock);
        }
        return "spike fail";
    }

    /*此种方式在分布式部署的项目中会出现多发券的情况*/
    @GetMapping("/syncLockSpike")
    public String syncLockSpike(){
        synchronized (this) {
            int stock = Integer.parseInt(redisUtil.get(RedisConstants.GOODS_STOCK_KEY).toString());
            if(stock > 0){
                stock--;
                log.info("剩余stock：{}", stock);
                redisUtil.set(RedisConstants.GOODS_STOCK_KEY, stock);
                return "success";
            }else {
                log.info("已抢购一空stock：{}", stock);
            }
        }
        return "spike fail";
    }

    /*使用redis锁来解决*/
    @GetMapping("/redisLockSpike")
    public String redisLockSpike(){
        String lockKeyVal = UUID.randomUUID().toString();
        //设置一把锁，用完销毁掉
        boolean lockRes = redisUtil.setNx(RedisConstants.GOODS_STOCK_LOCK_KEY, lockKeyVal, 10, TimeUnit.SECONDS);
        if(!lockRes)
            return "spike fail";
        try {
            int stock = Integer.parseInt(redisUtil.get(RedisConstants.GOODS_STOCK_KEY).toString());
            if(stock > 0){
                stock--;
                redisUtil.set(RedisConstants.GOODS_STOCK_KEY, stock);
                return "success";
            }else {
                log.info("已抢购一空stock：" + stock);
            }

        }finally {
            if(redisUtil.hasKey(RedisConstants.GOODS_STOCK_LOCK_KEY) && lockKeyVal.equals(redisUtil.get(RedisConstants.GOODS_STOCK_LOCK_KEY)))
                redisUtil.del(RedisConstants.GOODS_STOCK_LOCK_KEY);
        }
        return "spile fail";
    }


}
