package com.sb.cmpptohttp.service;


import com.sb.cmpptohttp.domain.result.Result;

/**
 * 发送短信
 * @author qiangchen
 */
public interface SmsService {

    /**
     * 发送短信消息
     *
     * @param mid
     * @param channelNo
     * @param mobile
     * @param content
     * @param extend
     * @return
     * @throws Exception
     */
    Result sendMsg(Long mid, String channelNo, String mobile, String content, String extend) throws Exception;
}
