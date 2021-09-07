package com.sb.cmpptohttp.handler;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.sb.cmpptohttp.domain.RedisKey;
import com.sb.cmpptohttp.domain.dto.ChannelMoMessage;
import com.sb.cmpptohttp.domain.dto.ChannelReportMessage;
import com.sb.cmpptohttp.domain.dto.ChannelSubmitLog;
import com.sb.cmpptohttp.domain.dto.MessageDTO;
import com.sb.cmpptohttp.domain.enums.SmsStatusEnum;
import com.sb.cmpptohttp.service.SendMsgService;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitRespMessage;
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
 * 电信SMGP 连接处理
 *
 * @author qiangchen
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class SMGPSessionConnectedHandler extends SessionConnectedHandler {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SendMsgService sendMsgService;

    /**
     * 提交记录日志
     */
    private static Logger submitLogger = LoggerFactory.getLogger("chan.submit");

    public SMGPSessionConnectedHandler() {

    }

    /**
     * 状态报告日志
     */
    private static Logger reportLogger = LoggerFactory.getLogger("chan.report");

    /**
     * 上行日志
     */
    private static Logger upLogger = LoggerFactory.getLogger("chan.upstream");

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("smgp msg: {}", msg);

        //获取 endpointEntity
        EndpointEntity endpointEntity = ctx.channel().attr(GlobalConstance.endpointEntityKey).get();

        if (msg instanceof SMGPDeliverMessage) {
            SMGPDeliverMessage e = (SMGPDeliverMessage) msg;

            log.info("smgpDeliverRequestMessage: {}", e);

            if (e.getFragments() != null) {
                log.info("e.getFragments count: {}", e.getFragments().size());
                //长短信会带有片断
                for (SMGPDeliverMessage frag : e.getFragments()) {
                    SMGPDeliverRespMessage responseMessage = new SMGPDeliverRespMessage();
                    responseMessage.setStatus(0);
                    responseMessage.setMsgId(frag.getMsgId());

                    log.info("long smgpDeliverResponseMessage: {}", JSONUtil.toJsonStr(responseMessage));

                    ctx.channel().write(responseMessage);
                }
            }

            SMGPDeliverRespMessage responseMessage = new SMGPDeliverRespMessage();
            responseMessage.setStatus(0);
            responseMessage.setMsgId(e.getReport().getMsgId());

            log.info("smgpDeliverResponseMessage: {}", JSONUtil.toJsonStr(responseMessage));

            ctx.channel().writeAndFlush(responseMessage);

            // 处理状态报告
            if (e.isReport()) {
                // 组装状态报告日志
                ChannelReportMessage channelReportMessage =
                        new ChannelReportMessage(e.getReport().getMsgId().toString(),
                                null,
                                1,
                                null,
                                e.getReport().getStat(),
                                SmsStatusEnum.getSmsStatus(e.getReport().getStat()),
                                // 码号
                                e.getDestTermId(),
                                // 通道号
                                endpointEntity.getId(),
                                System.currentTimeMillis(),
                                // 手机号
                                e.getSrcTermId(),
                                DateUtil.parse(e.getReport().getSubTime(), "yyMMddHHmm").getTime(),
                                System.currentTimeMillis(),
                                false);

                reportLogger.info(JSONUtil.toJsonStr(channelReportMessage));

                /**
                 * 更新短信记录状态
                 */
                updateSmsStatus(channelReportMessage);

            } else {
                // 处理上行短信
                String msgId = e.getMsgId().toString();
                short msgLength = Convert.toShort(e.getBMsgContent().length);
                String mobile = e.getSrcTermId();
                String content = e.getMsgContent();
                long timeStamp = e.getTimestamp();
                //码号
                String destId = e.getDestTermId();
                String channelNo = getEndpointEntity().getId();

                ChannelMoMessage channelMoMessage = new ChannelMoMessage(msgId, channelNo, msgLength, mobile, content, timeStamp,
                        destId);
                upLogger.info(JSONUtil.toJsonStr(channelMoMessage));

            }

        } else if (msg instanceof SMGPSubmitRespMessage) {
            SMGPSubmitRespMessage e = (SMGPSubmitRespMessage) msg;
            log.info("smgpSubmitResponseMessage: {}", e.toString());

            int sequenceId = e.getSequenceNo();

            Object obj = redisTemplate.opsForValue().get(RedisKey.SMS_SEQID_PREFIX + sequenceId);
            MessageDTO messageDTO = JSONUtil.toBean(obj.toString(), MessageDTO.class);

            log.info("sequenceId:{}, messageId: {}, mobile: {}", sequenceId, e.getMsgId().toString(),
                    messageDTO == null ? "" : messageDTO.getMobile());

            if (messageDTO != null) {

                // 下发成功打印日志，logtime
                ChannelSubmitLog channelSubmitLog = new ChannelSubmitLog(System.currentTimeMillis(), messageDTO.getMobile(),
                        e.getMsgId().toString(), messageDTO.getMid(),
                        messageDTO.getContent(), endpointEntity.getId());

                submitLogger.info(JSONUtil.toJsonStr(channelSubmitLog));

                // mid和msgid对应关系，key:msgId, value:mid + "_" + feeCount + "_" + appId
                redisTemplate.opsForValue().set(RedisKey.MSGID_MID_KEY_PREFIX + e.getMsgId().toString(),
                        messageDTO.getMid(), Duration.ofDays(4));

            } else {
                log.error("cannot find seqId: {}", sequenceId);
            }

        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 收到状态报告，更新短信记录状态
     */
    private void updateSmsStatus(ChannelReportMessage channelReportMessage) {
        String msgId = channelReportMessage.getMsgId();

        Object msgObject =
                redisTemplate.opsForValue().get(RedisKey.MSGID_MID_KEY_PREFIX + msgId);
        if (msgObject == null) {
            log.error("cannot find mid, msgid:{}", msgId);
            return;
        }

        Long mid = Convert.toLong(msgObject.toString());
        sendMsgService.updateMsgStatus(mid, channelReportMessage.getStatus(), channelReportMessage.getStatusCode());
    }

}
