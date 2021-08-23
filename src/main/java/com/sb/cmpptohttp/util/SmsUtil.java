package com.sb.cmpptohttp.util;

import com.sb.cmpptohttp.domain.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * 短信工具类
 *
 * @author chenqiang on 2021/8/19 8:53 上午
 */
public class SmsUtil {

    /**
     * 获取国内短信计费条数
     *
     * @param content
     * @return
     */
    public static Integer getSmsCount(String content) {

        int count = 1;
        if (StringUtils.isBlank(content)
                || content.length() <= Constants.SINGLE_SMS_LENGTH) {
            count = 1;
        } else {
            count = (int) Math.ceil((double) content.length() / Constants.LONG_SMS_PER_LENGTH);
        }
        return count;
    }
}
