package com.sb.cmpptohttp.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.sb.cmpptohttp.domain.enums.SmsStatusEnum;
import com.sb.cmpptohttp.entity.SendMsg;
import com.sb.cmpptohttp.mapper.SendMsgMapper;
import com.sb.cmpptohttp.service.SendMsgService;
import com.sb.cmpptohttp.util.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 短信记录服务
 *
 * @author chenqiang on 2021/8/18 11:25 上午
 */
@Service
@Slf4j
public class SendMsgServiceImpl implements SendMsgService {

    @Resource
    private SendMsgMapper sendMsgMapper;

    @Override
    public Boolean SaveSendMsg(Long mid, String channelNo, String mobile, String content, String extend) {

        SendMsg sendMsg = new SendMsg();
        sendMsg.setMid(mid);
        sendMsg.setChannelNo(channelNo);
        sendMsg.setMobile(mobile);
        sendMsg.setContent(content);
        sendMsg.setExtend(extend);
        sendMsg.setCount(SmsUtil.getSmsCount(content));
        sendMsg.setStatus(SmsStatusEnum.UNKNOWN.code);
        sendMsg.setSendDate(Convert.toInt(DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN)));

        try {
            sendMsgMapper.insert(sendMsg);

            return true;
        } catch (Exception e) {
            log.error("SaveSendMsg err", e);
            return false;
        }
    }

    @Override
    public Boolean updateMsgStatus(Long mid, Integer status, String statusCode) {

        int r = sendMsgMapper.updateMsgStatus(mid, status, statusCode);

        return true;
    }
}
