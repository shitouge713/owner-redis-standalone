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
         */
        rateLimiter.delete();
        rateLimiter.trySetRate(RateType.OVERALL, limit, timeout, RateIntervalUnit.SECONDS);
    }
}
