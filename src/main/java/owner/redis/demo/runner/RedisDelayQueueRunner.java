package owner.redis.demo.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import owner.redis.demo.enums.RedisDelayQueueEnum;
import owner.redis.demo.service.RedisDelayQueueService;
import owner.redis.demo.util.RedisDelayQueueUtils;
import owner.redis.demo.util.SpringUtil;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 启动延迟队列
 * Created by sxl on 2022/06/29.
 */
@Slf4j
@Component
public class RedisDelayQueueRunner implements CommandLineRunner {

    @Resource
    private SpringUtil springUtil;

    @Autowired
    private RedisDelayQueueUtils redisDelayQueueUtils;

    @Override
    public void run(String... args) {
        RedisDelayQueueEnum[] queueEnums = RedisDelayQueueEnum.values();
        Map<String, RedisDelayQueueService> runnerMap = new HashMap<>();
        for (RedisDelayQueueEnum queueEnum : queueEnums) {
            RedisDelayQueueService redisDelayQueueHandle = springUtil.getBean(queueEnum.getBeanId(), RedisDelayQueueService.class);
            runnerMap.put(queueEnum.getBeanId(), redisDelayQueueHandle);
        }
        for (RedisDelayQueueEnum queueEnum : queueEnums) {
            new Thread(() -> {
                while (true) {
                    try {
                        String value = redisDelayQueueUtils.getDelayQueue(queueEnum.getCode());
                        if (value != null) {
                            RedisDelayQueueService redisDelayQueueHandle = runnerMap.get(queueEnum.getBeanId());
                            redisDelayQueueHandle.executeTask(value);
                        }
                    } catch (InterruptedException e) {
                        log.error("fatalError,redis延迟队列异常中断,任务名称:{},e:", e);
                    } catch (Exception e) {
                        log.error("fatalError,redis延迟队列处理异常,任务名称:{},e:", e);
                    }
                }
            }).start();
        }
        log.info("redis延迟队列启动成功");
    }
}