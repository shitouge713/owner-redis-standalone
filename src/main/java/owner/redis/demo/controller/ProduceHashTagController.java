package owner.redis.demo.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import owner.redis.demo.vo.Result;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Api(tags = "redis模拟生成bigKey", consumes = "application/json")
@RequestMapping("/redis/hashTag")
public class ProduceHashTagController {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 在进行存储时，对应相同{user}的key，不管其他内容，都会存储在一个槽点上
     *
     * @return
     */
    @ApiOperation(value = "hashTag")
    @GetMapping("/hashTagMethod")
    public Result<Boolean> hashTagMethod() {
        String key = "myHashTag{user}:name";
        String key2 = "myHashTag{user}:age";
        redisTemplate.opsForValue().set(key, "yangguo");
        redisTemplate.opsForValue().set(key2, "45");
        return Result.success(true);
    }
}
