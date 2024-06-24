package owner.redis.demo.service;

import owner.redis.demo.request.DistributedLockRequest;


/**
 * 分布式锁场景
 */
public interface DistributedLockService {

    void distributedLockMethod(String orderNo);

    void redissonDistributedLock(String orderNo);

    boolean currentLimiting();

    boolean rankingList();

    boolean delayTask(DistributedLockRequest request);

    boolean slidingWindowA();

    /**
     * guava实现限流
     * @return
     */
    boolean slidingWindowB();
}
