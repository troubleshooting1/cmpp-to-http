package com.sb.cmpptohttp.domain;

/**
 * 通用类
 *
 * @author chenqiang
 */
public interface Constants {

    /**
     * 长短信每条短信最大字数
     */
    int LONG_SMS_PER_LENGTH = 67;

    /**
     * 单条短信每条短信最大字数
     */
    int SINGLE_SMS_LENGTH = 70;

    /**
     * 短信状态报告成功
     */
    String SMS_STAT_DELIVRD = "DELIVRD";

    /**
     * 短信状态报告未知
     */
    String SMS_STAT_UNKNOWN = "UNKNOWN";

    /**
     * src_id最大长度
     */
    Integer MAX_SRC_ID_LENGTH = 20;
}
