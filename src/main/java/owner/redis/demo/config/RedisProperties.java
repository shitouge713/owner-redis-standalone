package owner.redis.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description:
 * @create: 2022-06-17
 */
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    private String host;
    private String port;
    private String password;
    private int maxRediects;

    @Override
    public String toString() {
        return "RedisProperties{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", password='" + password + '\'' +
                ", maxRediects=" + maxRediects +
                '}';
    }
}