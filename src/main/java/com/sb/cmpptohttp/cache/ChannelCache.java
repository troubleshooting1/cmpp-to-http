package com.sb.cmpptohttp.cache;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sb.cmpptohttp.entity.Channel;
import com.sb.cmpptohttp.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Optional;

/**
 * 通道缓存
 *
 * 1分钟刷新一次
 *
 * @author qiangchen
 */
@Component
public class ChannelCache {

    @Autowired
    private ChannelService chanService;

    /**
     * key: channelNo，value: Channel实体类
     */
    private Cache<String, Optional<Channel>> channelCache;

    @PostConstruct
    private void init() {
        channelCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(1000)
                .recordStats()
                .build();
    }

    /**
     * 根据通道号获取通道
     * @param channelNo
     * @return
     */
    public Channel getChannelByChannelNo(String channelNo) {
        Optional<Channel> chanOptional = channelCache.get(
                channelNo,
                key -> Optional.ofNullable(chanService.getChannelByChannelNo(channelNo)));
        if (chanOptional == null || !chanOptional.isPresent()) {
            return null;
        }
        return chanOptional.get();
    }

    public void invalidate(String channelNo) {
        channelCache.invalidate(channelNo);
    }
}
