package com.sb.cmpptohttp.handler;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.sb.cmpptohttp.domain.RedisKey;
import com.sb.cmpptohttp.domain.dto.ChannelReportMessage;
import com.sb.cmpptohttp.domain.dto.ChannelSubmitLog;
import com.sb.cmpptohttp.domain.dto.MessageDTO;
import com.sb.cmpptohttp.service.SendMsgService;
import com.zx.sms.codec.sgip12.msg.*;
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
import java.util.List;

import static com.sb.cmpptohttp.domain.Constants.SMS_STAT_DELIVRD;

@Slf4j
@ChannelHandler.Sharable
@Component
public class SGIPSessionServerHandler extends SessionConnectedHandler {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SendMsgService sendMsgService;

    /**
     * 状态报告日志
     */
    private static Logger reportLogger = LoggerFactory.getLogger("chan.report");

    /**
     * 上行日志
     */
    private static Logger upLogger = LoggerFactory.getLogger("chan.upstream");

    /**
     * 提交记录日志
     */
    private static Logger submitLogger = LoggerFactory.getLogger("chan.submit");

    public SGIPSessionServerHandler() {

    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("sgip msg: {}", msg);

        //获取 endpointEntity
        EndpointEntity endpointEntity = ctx.channel().attr(GlobalConstance.endpointEntityKey).get();

        if (msg instanceof SgipDeliverRequestMessage) {
            SgipDeliverRequestMessage deli = (SgipDeliverRequestMessage) msg;
            SgipDeliverResponseMessage resp = new SgipDeliverResponseMessage(deli.getHeader());
            resp.setResult((short) 0);
            resp.setTimestamp(deli.getTimestamp());

            List<SgipDeliverRequestMessage> deliarr = deli.getFragments();
            if (deliarr != null) {
                for (SgipDeliverRequestMessage item : deliarr) {
                    SgipDeliverResponseMessage item_resp = new SgipDeliverResponseMessage(item.getHeader());
                    item_resp.setResult((short) 0);
                    item_resp.setTimestamp(item.getTimestamp());
                    ctx.writeAndFlush(item_resp);
                }
            }
            ctx.writeAndFlush(resp);
        } else if (msg instanceof SgipSubmitRequestMessage) {
            SgipSubmitRequestMessage submit = (SgipSubmitRequestMessage) msg;
            log.info("SgipSubmitRequestMessage: {}", submit);

        } else if (msg instanceof SgipSubmitResponseMessage) {

            SgipSubmitResponseMessage e = (SgipSubmitResponseMessage) msg;
            log.info("SgipSubmitResponseMessage: {}", JSONUtil.toJsonStr(e));

            int sequenceId = e.getSequenceNo();

            Object obj = redisTemplate.opsForValue().get(RedisKey.SMS_SEQID_PREFIX + sequenceId);
            MessageDTO messageDTO = JSONUtil.toBean(obj.toString(), MessageDTO.class);

            log.info("seqNo: {}, seqNum: {}", e.getSequenceNo(), e.getSequenceNumber());
            log.info("sequenceId:{}, messageId: {}, mobile: {}", sequenceId, e.getSequenceNumber(),
                    messageDTO == null ? "" : messageDTO.getMobile());

            if (messageDTO != null) {

                // 下发成功打印日志，logtime
                ChannelSubmitLog channelSubmitLog = new ChannelSubmitLog(System.currentTimeMillis(), messageDTO.getMobile(),
                        String.valueOf(sequenceId), messageDTO.getMid(),
                        messageDTO.getContent(), endpointEntity.getId());

                submitLogger.info(JSONUtil.toJsonStr(channelSubmitLog));

                // mid和msgid对应关系，key:msgId, value:mid + "_" + feeCount + "_" + appId
                redisTemplate.opsForValue().set(RedisKey.MSGID_MID_KEY_PREFIX + e.getSequenceNumber(),
                        messageDTO.getMid(), Duration.ofDays(4));

            } else {
                log.error("cannot find seqId: {}", sequenceId);
            }

        } else if (msg instanceof SgipReportRequestMessage) {
            log.info("返回的状态报告"+JSONUtil.toJsonStr(msg));
            SgipReportRequestMessage reportRequestMessage = (SgipReportRequestMessage) msg;
            log.info("SgipReportRequestMessage: {}", JSONUtil.toJsonStr(reportRequestMessage));

            log.info("seqNo: {}, seqId: {}, seqId2: {}, seqNum: {}", reportRequestMessage.getSequenceNo(),
                    reportRequestMessage.getSequenceId().toString(),
                    reportRequestMessage.getUsernumber(),
                    reportRequestMessage.getSequenceNumber().toString());
            SgipReportResponseMessage resp =
                    new SgipReportResponseMessage(((SgipReportRequestMessage) msg).getHeader());
            resp.setResult((short) 0);
            resp.setTimestamp(((SgipReportRequestMessage) msg).getTimestamp());
            ctx.channel().writeAndFlush(resp);

            // SGIP状态枚举，state, 0：发送成功，1：等待发送，2：发送失败
            int status = 0;
            if (Convert.toInt(reportRequestMessage.getState()) == 0) {
                status = 1;
            } else if (Convert.toInt(reportRequestMessage.getState()) == 2) {
                status = 2;
            }

            // 状态报告处理， SGIP状态报告成功时ErrorCode为0
            ChannelReportMessage channelReportMessage =
                    new ChannelReportMessage(reportRequestMessage.getSequenceId().toString(),
                            null,
                            1,
                            null,
                            status == 1 ? SMS_STAT_DELIVRD : String.valueOf(reportRequestMessage.getErrorcode()),
                            status,
                            "",
                            // 通道号
                            endpointEntity.getId(),
                            System.currentTimeMillis(),
                            reportRequestMessage.getUsernumber(),
                            System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            false);

            reportLogger.info(JSONUtil.toJsonStr(channelReportMessage));

            /**
             * 更新短信记录状态
             */
            updateSmsStatus(channelReportMessage);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 收到状态报告，更新短信记录状态
     */
    private void updateSmsStatus(ChannelReportMessage channelReportMessage) {
        String msgId = channelReportMessage.getMsgId();

        //RedisTemplate redisTemplateUpdate = new RedisTemplate();
        //redisTemplateUpdate.afterPropertiesSet();
        Object msgObject = redisTemplate.opsForValue().get(RedisKey.MSGID_MID_KEY_PREFIX + msgId);
        if (msgObject == null) {
            log.error("cannot find mid, msgid:{}", msgId);
            return;
        }

        Long mid = Convert.toLong(msgObject);
        sendMsgService.updateMsgStatus(mid, channelReportMessage.getStatus(), channelReportMessage.getStatusCode());
    }
}
