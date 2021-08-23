package com.sb.cmpptohttp.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/**
 * @author chenqiang on 2021/8/18 10:39 上午
 */
@Component
public class IdGenerateUtil {

    /**
     * 参数1为终端ID
     * 参数2为数据中心ID
     */
    Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    public Long getUniqueId() {
        return snowflake.nextId();
    }
}
