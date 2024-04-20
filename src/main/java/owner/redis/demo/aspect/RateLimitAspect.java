package owner.redis.demo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import owner.redis.demo.annotation.MyRateLimit;
import owner.redis.demo.exception.NoWarnException;

import java.lang.reflect.Method;

/**
 * AOP方式实现动态限流
 */
@Aspect
@Component
public class RateLimitAspect {
    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(owner.redis.demo.annotation.MyRateLimit)")
    public void rateLimit() {
    }

    @Around("rateLimit()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MyRateLimit rateLimit = method.getAnnotation(MyRateLimit.class);
        String key = rateLimit.key();
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        if (rateLimiter.tryAcquire()) {
            return joinPoint.proceed();
        } else {
            throw new NoWarnException("动态限流已达上限，请稍后再试");
        }
    }
}
