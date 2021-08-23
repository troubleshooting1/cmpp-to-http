package com.sb.cmpptohttp.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 短信上行日志
 *
 * @author chenqiang on 2020/9/13 17:26
 */
@Getter
@Setter
public class ChannelMoMessage {

    public ChannelMoMessage(String msgId, String channelNo, short msgLength, String mobile, String content, Long logTime,
                            String destId) {
        this.msgId = msgId;
        this.channelNo = channelNo;
        this.msgLength = msgLength;
        this.mobile = mobile;
        this.content = content;
        this.logTime = logTime;
        this.destId = destId;
    }

    /**
     * msgid
     */
    String msgId;

    /**
     * 通道号
     */
    String channelNo;

    /**
     * 上行长度
     */
    short msgLength;

    /**
     * 手机号
     */
    String mobile;

    /**
     * 上行短信内容
     */
    String content;

    /**
     * 记录时间
     */
    Long logTime;

    /**
     * 码号
     */
    String destId;
}
