package com.sb.cmpptohttp.handler;


import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.sb.cmpptohttp.domain.RedisKey;
import com.sb.cmpptohttp.domain.dto.ChannelReportMessage;
import com.sb.cmpptohttp.domain.dto.ChannelSubmitLog;
import com.sb.cmpptohttp.domain.dto.MessageDTO;
import com.sb.cmpptohttp.domain.enums.SmsStatusEnum;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.DeliverSmResp;
import com.zx.sms.codec.smpp.msg.SubmitSmResp;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 国际SMPP 连接处理
 *
 * @author qiangchen
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class SMPPSessionConnectedHandler extends SessionConnectedHandler {

    @Autowired
    private RedisTemplate redisTemplate;
//    private static RedisTemplate redisTemplate = (RedisTemplate) SpringContextUtils.getBean("redisTemplate");

    /**
     * 状态报告日志
     */
    private static Logger reportLogger = LoggerFactory.getLogger("chan.report");

    /**
     * 上行日志
     */
    private static Logger upLogger = LoggerFactory.getLogger("chan.upstream");

    /**
     * cmpp提交记录日志
     */
    private static Logger submitLogger = LoggerFactory.getLogger("chan.submit");

    public SMPPSessionConnectedHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("smpp msg: {}", msg.toString());

        //获取 endpointEntity
        EndpointEntity endpointEntity = ctx.channel().attr(GlobalConstance.endpointEntityKey).get();

        if (msg instanceof SubmitSmResp) {
            SubmitSmResp smResp = (SubmitSmResp) msg;

            int sequenceId = smResp.getSequenceNo();

            Object obj = redisTemplate.opsForValue().get(RedisKey.SMS_SEQID_PREFIX + sequenceId);
            MessageDTO messageDTO = JSONUtil.toBean(obj.toString(), MessageDTO.class);

            log.info("sequenceId:{}, messageId: {}, messageDTO: {}", sequenceId, smResp.getMessageId(), messageDTO);

            if (messageDTO != null) {


                // 下发成功打印日志，logtime
                ChannelSubmitLog channelSubmitLog = new ChannelSubmitLog(System.currentTimeMillis(), messageDTO.getMobile(),
                        smResp.getMessageId(), messageDTO.getMid(), messageDTO.getContent(), endpointEntity.getId());

                submitLogger.info(JSONUtil.toJsonStr(channelSubmitLog));

                // mid和msgid对应关系，key:msgId, value:mid + "_" + feeCount + "_" + appId
                redisTemplate.opsForValue().set(RedisKey.MSGID_MID_KEY_PREFIX + smResp.getMessageId(),
                        messageDTO.getMid() , Duration.ofDays(4));

            } else {
                log.error("cannot find seqId: {}", sequenceId);
            }

            ctx.writeAndFlush(msg);
        } else if (msg instanceof DeliverSmReceipt) {
            DeliverSmReceipt e = (DeliverSmReceipt) msg;

            DeliverSmResp res = e.createResponse();
            res.setMessageId(String.valueOf(System.currentTimeMillis()));
            ctx.writeAndFlush(res);

            log.info("DeliverSmReceipt: {}", e);

            log.info("e, e.getMsgContent(): {}, e.getSourceAddress().getAddress(): {}", e.getMsgContent(),
                    e.getSourceAddress().getAddress());

            // 组装状态报告日志
            ChannelReportMessage channelReportMessage =
                    new ChannelReportMessage(e.getId(),
                            null,
                            1,
                            null,
                            e.getStat(),
                            SmsStatusEnum.getSmsStatus(e.getStat()),
                            // 码号
                            "",
                            // 通道号
                            endpointEntity.getId(),
                            System.currentTimeMillis(),
                            // 手机号
                            e.getSourceAddress().getAddress(),
                            DateUtil.parse(e.getSubmit_date(), "yyMMddHHmm").getTime(),
                            System.currentTimeMillis(),
                            true);

            reportLogger.info(JSONUtil.toJsonStr(channelReportMessage));


        } else if (msg instanceof DeliverSm) {
            DeliverSm e = (DeliverSm) msg;
            log.info("DeliverSm: {}", e);

        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
