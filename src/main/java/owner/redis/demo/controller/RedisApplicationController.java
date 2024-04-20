package owner.redis.demo.controller;

import com.google.common.util.concurrent.RateLimiter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import owner.redis.demo.annotation.MyRateLimit;
import owner.redis.demo.request.DistributedLockRequest;
import owner.redis.demo.request.RedisTypeRequest;
import owner.redis.demo.service.DistributedLockService;
import owner.redis.demo.util.OrderNoGenerator;
import owner.redis.demo.vo.Result;

import javax.validation.Valid;

@Slf4j
@RestController
@Api(tags = "redis应用", consumes = "application/json")
@RequestMapping("/redis/application")
public class RedisApplicationController {
    @Autowired
    private DistributedLockService distributedLockService;
    @Autowired
    private OrderNoGenerator orderNoGenerator;
    //2.0表示qps
    RateLimiter rateLimiter = RateLimiter.create(2.0);

    @ApiOperation(value = "应用场景-分布式锁-RedisTemplate实现")
    @GetMapping("/distributedLock")
    public Result<Boolean> distributedLock(@Valid DistributedLockRequest request) {
        distributedLockService.distributedLockMethod(request.getOrderNo());
        return Result.success();
    }

    @ApiOperation(value = "应用场景-分布式锁-Redisson实现")
    @GetMapping("/redissonDistributedLock")
    public Result<Boolean> redissonDistributedLock(@Valid DistributedLockRequest request) {
        distributedLockService.redissonDistributedLock(request.getOrderNo());
        return Result.success();
    }

    @ApiOperation(value = "应用场景-生成分布式id")
    @GetMapping("/generateDistributedIds")
    public Result<String> generateDistributedIds(@Valid RedisTypeRequest request) {
        String orderNo;
        if (request.getType() == 1) {
            orderNo = orderNoGenerator.generateIdsByRedisson("ORDER");
        } else {
            orderNo = orderNoGenerator.generateOrderNumber("ORDER", 3);
        }
        return Result.success(orderNo);
    }

    @ApiOperation(value = "应用场景-限流")
    @GetMapping("/currentLimiting")
    public Result<Boolean> currentLimiting() {
        return Result.success(distributedLockService.currentLimiting());
    }

    @ApiOperation(value = "应用场景-延迟队列")
    @GetMapping("/delayTask")
    public Result<Boolean> delayTask(@Valid DistributedLockRequest request) {
        return Result.success(distributedLockService.delayTask(request));
    }

    /**
     * 借助@MyRateLimit实现限流
     * @return
     */
    @MyRateLimit(key = "myRateLimiter")
    @ApiOperation(value = "应用场景-滑动窗口限流A")
    @GetMapping("/slidingWindowA")
    public Result<Boolean> slidingWindowA() {
        return Result.success(distributedLockService.slidingWindowA());
    }

    /**
     * guava实现限流
     * 没有redisson配置丰富，只支持秒级别的，能做到滑动窗口么？
     *
     * @return
     */
    @ApiOperation(value = "应用场景-滑动窗口限流B")
    @GetMapping("/slidingWindowB")
    public Result<Boolean> slidingWindowB() {
        if (rateLimiter.tryAcquire()) {
            return Result.success(distributedLockService.slidingWindowB());
        } else {
            return Result.failed("接口限流");
        }
    }


}
