package owner.redis.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 容器启动后配置key=myRateLimiter的限流策略
 */
@Slf4j
@Component
public class MyRateLimitConfig implements ApplicationRunner {

    @Value("${rateLimiter.limit:10}")
    private Integer limit;
    @Value("${rateLimiter.key:myRateLimiter}")
    private String key;
    @Value("${rateLimiter.timeout:50}")
    private Long timeout;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(ApplicationArguments args) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        /**
         * 保证先删除，再设置，才会对修改的配置生效
         * RateType.OVERALL：所有客户端加总限流，就是集群下所有的流量
         * RateType.PER_CLIENT：每个客户端单独计算流量，每台机器的流量
         */
        // 参数1 type：限流类型，可以是自定义的任何类型，用于区分不同的限流策略。
        // 参数2 rate：限流速率，即单位时间内允许通过的请求数量。
        // 参数3 rateInterval：限流时间间隔，即限流速率的计算周期长度。
        // 参数4 unit：限流时间间隔单位，可以是秒、毫秒等。
        rateLimiter.delete();
        rateLimiter.trySetRate(RateType.OVERALL, limit, timeout, RateIntervalUnit.SECONDS);
    }
}
