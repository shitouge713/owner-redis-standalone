package owner.redis.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

@Slf4j
public class RedissonUtils {

    /**
     * 优雅释放lock锁的方式
     *
     * @param lock
     */
    public static void unlock(RLock lock) {
        //如果非当前线程尝试释放锁，会报错
        //lock.unlock();不可直接释放，需要判断下线程是否属于当前线程后，再释放
        if (lock.isLocked()) { // 是否还是锁定状态，可能因为过期已经释放了
            if (lock.isHeldByCurrentThread()) { // 时候是当前执行线程的锁
                lock.unlock(); // 释放锁
            }
        }
    }
}
