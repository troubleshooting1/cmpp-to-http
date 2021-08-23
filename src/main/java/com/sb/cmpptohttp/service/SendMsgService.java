package com.sb.cmpptohttp.service;

/**
 * 短信记录服务
 *
 * @author chenqiang on 2021/8/18 11:25 上午
 */
public interface SendMsgService {

    /**
     * 保存短信记录
     * @param mid
     * @param channelNo
     * @param mobile
     * @param content
     * @param extend
     * @return
     */
    Boolean SaveSendMsg(Long mid, String channelNo, String mobile, String content, String extend);

    /**
     * 更新短信记录状态
     * @param mid
     * @param status
     * @param statusCode
     * @return
     */
    Boolean updateMsgStatus(Long mid, Integer status, String statusCode);
}
