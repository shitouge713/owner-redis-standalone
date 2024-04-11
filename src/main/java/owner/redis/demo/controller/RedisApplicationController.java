package owner.redis.demo.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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


}
