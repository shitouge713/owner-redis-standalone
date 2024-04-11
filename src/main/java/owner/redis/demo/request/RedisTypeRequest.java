package owner.redis.demo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@EqualsAndHashCode
@ApiModel(description = "使用哪种redis客户端")
public class RedisTypeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "redis客户端类型不可为空")
    @ApiModelProperty(value = "0 redisTemplate|1 redisson")
    private Integer type;
}
