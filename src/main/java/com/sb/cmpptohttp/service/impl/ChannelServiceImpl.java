package com.sb.cmpptohttp.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.sb.cmpptohttp.entity.Channel;
import com.sb.cmpptohttp.mapper.ChannelMapper;
import com.sb.cmpptohttp.service.ChannelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 短信通道信息表 服务实现类
 *
 * @author chenqiang
 */
@Service
public class ChannelServiceImpl implements ChannelService {

    @Resource
    private ChannelMapper channelMapper;

    @Override
    public Channel getChannelByChannelNo(String channelNo) {
        QueryWrapper<Channel> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(Channel::getChannelNo, channelNo);
        Channel channel = channelMapper.selectOne(queryWrapper);

        return channel;
    }
}
