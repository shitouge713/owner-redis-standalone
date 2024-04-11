package owner.redis.demo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import owner.redis.demo.constant.RedisConfig;
import owner.redis.demo.enums.RedisDelayQueueEnum;
import owner.redis.demo.enums.ReturnStatusEnum;
import owner.redis.demo.exception.NoWarnException;
import owner.redis.demo.request.DistributedLockRequest;
import owner.redis.demo.service.DistributedLockService;
import owner.redis.demo.util.RedisDelayQueueUtils;
import owner.redis.demo.util.RedisUtils;
import owner.redis.demo.util.RedissonUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisDelayQueueUtils redisDelayQueueUtils;

    @Override
    public void distributedLockMethod(String orderNo) {
        //模拟逻辑执行过程
        try {
            Boolean isLock = redisUtils.getLock(RedisConfig.KEY_ORDER_NO + orderNo, "1", 10L);
            if (!isLock) {
                log.warn("获取分布式锁失败,orderNo:{}", orderNo);
                throw new NoWarnException(ReturnStatusEnum.FAIL_GET_DISTRIBUTED_LOCK);
            }
            log.info(Thread.currentThread().getName() + "，获取到锁");
            Thread.sleep(10000);
            //正常执行结束释放
            redisUtils.releaseLock(RedisConfig.KEY_ORDER_NO + orderNo, "1");
        } catch (InterruptedException e) {
            //非获取锁失败的异常，释放
            redisUtils.releaseLock(RedisConfig.KEY_ORDER_NO + orderNo, "1");
            log.error("Thread.sleep异常,{}", e);
            throw new NoWarnException("Thread.sleep异常");
        }
    }

    @Override
    public void redissonDistributedLock(String orderNo) {
        //模拟逻辑执行过程
        log.info(Thread.currentThread().getName() + "，尝试获取锁");
        RLock lock = redissonClient.getLock(RedisConfig.REDISSON_KEY_ORDER_NO + orderNo);
        try {
            //第一个参数表示获取不到锁，锁等待的时间，此时默认的锁过期时间是30s，此时看门狗会生效
            //boolean isLocked = lock.tryLock(3, TimeUnit.SECONDS);
            //如果想自定义锁过期时间，需要设置两个时间参数,第一个表示锁等待时间，第二个表示超时时间，只要不是30s，看门狗就不会生效
            //如果超时时间设置为30s时，看门狗也不会生效，过期时间必须使用默认的才可以
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取分布式锁失败,orderNo:{}", orderNo);
                throw new NoWarnException(ReturnStatusEnum.FAIL_GET_DISTRIBUTED_LOCK);
            }
            log.info(Thread.currentThread().getName() + "，获取到锁");
            Thread.sleep(2000);
            //正常执行结束释放
            log.info(Thread.currentThread().getName() + "，准备释放锁");
            RedissonUtils.unlock(lock);
        } catch (InterruptedException e) {
            RedissonUtils.unlock(lock);
            log.error("Thread.sleep异常,{}", e);
            throw new NoWarnException("Thread.sleep异常");
        }
    }

    // 限流的个数
    private int maxCount = 4;
    // 指定的时间内
    private long interval = 10;
    // 原子类计数器
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    // 起始时间
    private long startTime = System.currentTimeMillis();

    @Override
    public boolean currentLimiting() {
        atomicInteger.addAndGet(1);
        /*if (atomicInteger.get() == 1) {
            startTime = System.currentTimeMillis();
            atomicInteger.addAndGet(1);
            return true;
        }*/
        // 超过了间隔时间，直接重新开始计数
        if (System.currentTimeMillis() - startTime > interval * 1000) {
            startTime = System.currentTimeMillis();
            atomicInteger.set(1);
            return true;
        }
        // 还在间隔时间内,check有没有超过限流的个数
        if (atomicInteger.get() > maxCount) {
            return false;
        }
        return true;
    }

    @Override
    public boolean delayTask(DistributedLockRequest request) {
        redisDelayQueueUtils.addDelayQueue(request.getOrderNo(), 10, TimeUnit.SECONDS, RedisDelayQueueEnum.TEST_DELAYED_TASK.getCode());
        return true;
    }
}
