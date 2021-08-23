package com.sb.cmpptohttp.service;

import com.sb.cmpptohttp.entity.Channel;


/**
 * 短信通道信息表 服务类
 *
 * @author chenqiang
 */
public interface ChannelService {

    /**
     * 根据通道号获取通道
     * @param channelNo
     * @return
     */
    Channel getChannelByChannelNo(String channelNo);
}
