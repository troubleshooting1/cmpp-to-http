package com.sb.cmpptohttp.domain.dto;

import lombok.Data;

/**
 * cmpp 提交记录日志
 * @author chenqiang on 2020/9/12 22:34
 */
@Data
public class ChannelSubmitLog {

    public ChannelSubmitLog(Long logTime, String mobile, String msgId, Long mid, String content, String channelNo) {
        this.logTime = logTime;
        this.mobile = mobile;
        this.msgId = msgId;
        this.mid = mid;
        this.content = content;
        this.channelNo = channelNo;
    }

    /**
     * 日志时间
     */
    private Long logTime;

    /**
     * 发送手机号
     */
    private String mobile;

    /**
     * 运营商返回的短信Id
     */
    private String msgId;

    /**
     * 我们平台产生的短信id
     */
    private Long mid;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 通道号
     */
    private String channelNo;
}
