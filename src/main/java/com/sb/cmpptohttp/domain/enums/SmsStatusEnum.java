package com.sb.cmpptohttp.domain.enums;

import cn.hutool.core.collection.CollectionUtil;
import com.sb.cmpptohttp.domain.Constants;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短信发送状态
 *
 * @author chenqiang
 */
public enum SmsStatusEnum {
    UNKNOWN(0, "未知"),
    SUCCESS(1, "成功"),
    FAIL(2, "失败");

    public Integer code;

    public String name;

    SmsStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据运营商返回状态获取短信状态
     *
     * @param stat
     * @return
     */
    public static Integer getSmsStatus(String stat) {
        if (Constants.SMS_STAT_DELIVRD.equals(stat)) {
            return SmsStatusEnum.SUCCESS.code;
        } else if (Constants.SMS_STAT_UNKNOWN.equals(stat)) {
            return SmsStatusEnum.UNKNOWN.code;
        } else {
            return SmsStatusEnum.FAIL.code;
        }
    }

}
