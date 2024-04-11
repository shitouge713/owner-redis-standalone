package owner.redis.demo.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单发号器服务
 * 业务编码+年的后 2 位+月+日+分 + 订单数，固定长度为18
 * 1分钟支持生成1万个
 *
 * @author jsrf
 * @date 2022/10/13 4:53 PM
 */
@Slf4j
@Service
public class OrderNoGenerator {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RedissonClient redissonClient;

    @Value("${spring.profiles.active}")
    private String curEnvCode;

    private static final String ID_KEY = "xiuji:";
    private static final int BASE_36 = 36;
    private static final Integer SEQUENCE_LENGTH = 5;

    /**
     * 基于redisson生成全局唯一id
     *
     * @param prefix
     * @return
     */
    public String generateIdsByRedisson(String prefix) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMM");
            String dateStr = LocalDate.now().format(formatter);
            //使用了Redisson的AtomicLong对象生成唯一序列号
            RAtomicLong atomicLong = redissonClient.getAtomicLong(ID_KEY + dateStr);
            //设置过期时间为20S
            if (atomicLong.get() == 0) {
                atomicLong.expire(20, TimeUnit.SECONDS);
            }
            //将唯一序列号转换为36进制的字符串，长度为4位，用于减少ID的长度
            String sequenceStr = Long.toString(atomicLong.incrementAndGet(), BASE_36).toUpperCase();
            //36进制的序列号若小于4位，则用0补齐高位
            if (sequenceStr.length() < SEQUENCE_LENGTH) {
                sequenceStr = String.format("%4s", sequenceStr).replace(' ', '0');
                ;
            }
            String serialId = dateStr + sequenceStr;
            log.info("生成的工单号：{}", dateStr + sequenceStr);
            return dateStr + sequenceStr;
        } catch (Exception e) {
            log.error("fatalError,generateOrderNumber,生成订单号异常:", e);
            return generateOrderNumber(prefix);
        }
    }

    /**
     * 分钟级别的id自增
     *
     * @param prefix
     * @param sourceType
     * @return
     */
    public String generateOrderNumber(String prefix, Integer sourceType) {
        try {
            Assert.notBlank(prefix);
            Assert.notNull(sourceType);
            String sourceTypeStr = StringUtils.leftPad(String.valueOf(sourceType), 2, "0");

            //获取当前时间
            Date now = new Date();
            String yyMMdd = DateUtil.format(now, "yyMMdd");
            int minute = DateUtil.hour(now, true) * 60 + DateUtil.minute(now);
            String orderNoPrefix = prefix + sourceTypeStr + yyMMdd + StringUtils.leftPad(String.valueOf(minute), 4, "0");

            Long value = redisUtils.getAutoIncrementNumberAndExpire("order:", orderNoPrefix, 2L, TimeUnit.MINUTES);
            return orderNoPrefix + StringUtils.leftPad(String.valueOf(value), 4, "0");
        } catch (Exception e) {
            log.error("fatalError,generateOrderNumber,生成订单号异常:", e);
            return generateOrderNumber(prefix);
        }
    }


    public String generateNumberContainsActive(String prefix, Integer sourceType) {
        try {
            Assert.notBlank(prefix);
            Assert.notNull(sourceType);
            String sourceTypeStr = StringUtils.leftPad(String.valueOf(sourceType), 2, "0");
            //获取当前时间
            Date now = new Date();
            String yyMMdd = DateUtil.format(now, "yyMMdd");
            int minute = DateUtil.hour(now, true) * 60 + DateUtil.minute(now);
            Integer curEnvCodeInt = 0;
            if (StringUtils.isNotBlank(curEnvCode)) {
                switch (curEnvCode) {
                    case "local":
                        curEnvCodeInt = 4;
                        break;
                    case "test":
                        curEnvCodeInt = 3;
                        break;
                    case "pre":
                        curEnvCodeInt = 2;
                        break;
                    case "prod":
                        curEnvCodeInt = 1;
                        break;
                }
            }
            String orderNoPrefix = prefix + sourceTypeStr + yyMMdd + StringUtils.leftPad(String.valueOf(minute), 4, "0") + curEnvCodeInt;
            Long value = redisUtils.getAutoIncrementNumberAndExpire("order:", orderNoPrefix, 2L, TimeUnit.MINUTES);
            return orderNoPrefix + StringUtils.leftPad(String.valueOf(value), 3, "0");
        } catch (Exception e) {
            log.error("fatalError,generateOrderNumber,生成订单号异常:", e);
            return generateOrderNumber(prefix);
        }
    }

    private static final String[] beforeShuffle = new String[]{
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    };

    public static String generateOrderNumber(String pref) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String time = formatter.format(now);
        String r = genRandom(5);
        return pref + time + r;
    }

    /**
     * 生成num个随机（数字和字母组合）串
     *
     * @param num 1到36之间
     * @return num不合法返回null
     */
    public static String genRandom(int num) {
        try {
            if (num <= 0 || num > 36) {
                return null;
            }
            List<String> list = Arrays.asList(beforeShuffle);
            Collections.shuffle(list);
            return StringUtils.join(list.subList(0, num).toArray());
        } catch (Exception e) {
            log.error("fatalError,OrderNoUtil,生成随机数异常:", e);
        }
        return "";
    }

}
