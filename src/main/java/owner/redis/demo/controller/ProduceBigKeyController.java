package owner.redis.demo.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import owner.redis.demo.util.RedisUtils;
import owner.redis.demo.vo.Result;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "redis模拟生成bigKey", consumes = "application/json")
@RequestMapping("/redis/bigKey")
public class ProduceBigKeyController {

    private int targetSize = 10 * 1024 * 1024; // 10MB
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @ApiOperation(value = "String-BigKey")
    @GetMapping("/stringBigKey")
    public Result<Boolean> stringBigKey() {
        StringBuilder sb = new StringBuilder();
        // 每次追加一个字符 'a' 直到达到目标大小
        while (sb.toString().getBytes().length < targetSize) {
            sb.append('a');
        }
        redisUtils.set("string", sb.toString());
        return Result.success();
    }

    @ApiOperation(value = "List-BigKey")
    @GetMapping("/listBigKey")
    public Result<Boolean> listBigKey() {
        List<String> list = new ArrayList<>();
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        // 每次追加一个字符 'a' 直到达到目标大小
        int i = 1;
        StringBuilder sb = new StringBuilder();
        while (list.toString().getBytes().length < targetSize) {
            sb.append('a');
            listOps.rightPush("myList", "a" + i++);
        }
        return Result.success();
    }

    @ApiOperation(value = "Zset-BigKey")
    @GetMapping("/zSetBigKey")
    public Result<Boolean> zSetBigKey() {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        StringBuilder sb = new StringBuilder();
        // 每次追加一个字符 'a' 直到达到目标大小
        int i = 1;
        while (sb.toString().getBytes().length < targetSize) {
            sb.append('a');
            zSetOps.add("myZSet", "member" + i, i++);
        }
        return Result.success();
    }

    @ApiOperation(value = "Hash-BigKey")
    @GetMapping("/hashBigKey")
    public Result<Boolean> hashBigKey() {
        HashOperations<String, String, String> hashOpts = redisTemplate.opsForHash();
        StringBuilder sb = new StringBuilder();
        // 每次追加一个字符 'a' 直到达到目标大小
        int i = 1;
        while (sb.toString().getBytes().length < targetSize) {
            sb.append('a');
            hashOpts.put("myHash", "field" + i, "value" + i);
            i++;
        }
        return Result.success();
    }
}
