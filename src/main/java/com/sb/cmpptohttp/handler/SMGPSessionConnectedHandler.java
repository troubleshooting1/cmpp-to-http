package com.sb.cmpptohttp.handler;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.sb.cmpptohttp.domain.dto.ChannelMoMessage;
import com.sb.cmpptohttp.domain.dto.ChannelReportMessage;
import com.sb.cmpptohttp.domain.enums.SmsStatusEnum;
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
import org.springframework.stereotype.Component;

/**
 * 电信SMGP 连接处理
 *
 * @author qiangchen
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class SMGPSessionConnectedHandler extends SessionConnectedHandler {
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
            responseMessage.setMsgId(e.getMsgId());

            log.info("smgpDeliverResponseMessage: {}", JSONUtil.toJsonStr(responseMessage));

            ctx.channel().writeAndFlush(responseMessage);

            // 处理状态报告
            if (e.isReport()) {
                // 组装状态报告日志
                ChannelReportMessage channelReportMessage =
                        new ChannelReportMessage(e.getMsgId().toString(),
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
                                DateUtil.parse(e.getRecvTime(), "yyMMddHHmm").getTime(),
                                false);

                reportLogger.info(JSONUtil.toJsonStr(channelReportMessage));


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

            int sequenceId = e.getMsgId().getSequenceId();

        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
