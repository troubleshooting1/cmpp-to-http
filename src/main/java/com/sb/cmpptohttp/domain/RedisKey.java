package com.sb.cmpptohttp.domain;

/**
 * @author chenqiang on
 */
public interface RedisKey {

    /**
     * 短信提交seqId，根据seqId找到对应的mid，key: seqId, value: messageDTO
     */
    String SMS_SEQID_PREFIX = "seqId:";

    /**
     * cmpp客户端mid和msgid对应关系key，key: msgid
     */
    String MSGID_MID_KEY_PREFIX = "msgid:mid:";
}
