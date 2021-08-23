package com.sb.cmpptohttp.domain.form;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenqiang on 2020/9/5 23:08
 */
@Data
public class SendMsgForm {

    /**
     * 通道号
     */
    private String channelNo;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 用户自带扩展号
     */
    private String extend;
}
