package com.sb.cmpptohttp.handler;


import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.sb.cmpptohttp.domain.dto.ChannelReportMessage;
import com.zx.sms.codec.sgip12.msg.*;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 联通SGIP 连接处理
 *
 * @author qiangchen
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class SGIPSessionConnectedHandler extends SessionConnectedHandler {


    /**
     * 状态报告日志
     */
    private static Logger reportLogger = LoggerFactory.getLogger("chan.report");

    /**
     * 上行日志
     */
    private static Logger upLogger = LoggerFactory.getLogger("chan.upstream");

    public SGIPSessionConnectedHandler() {

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

//            SgipSubmitResponseMessage resp = new SgipSubmitResponseMessage(submit.getHeader());
//            resp.setTimestamp(submit.getTimestamp());
//            resp.setResult((short) 0);
//
//            boolean sendreport = 1 == submit.getReportflag();
//
//            List<SgipSubmitRequestMessage> deliarr = submit.getFragments();
//            if (deliarr != null) {
//                for (SgipSubmitRequestMessage item : deliarr) {
//                    SgipSubmitResponseMessage item_resp = new SgipSubmitResponseMessage(item.getHeader());
//                    item_resp.setResult((short) 0);
//                    item_resp.setTimestamp(item.getTimestamp());
//                    ctx.writeAndFlush(item_resp);
//
//                    if (sendreport) {
//                        SgipReportRequestMessage report = new SgipReportRequestMessage();
//                        report.setSequenceId(item_resp.getSequenceNumber());
//                        ctx.writeAndFlush(report);
//                    }
//                }
//            }
//
//            ChannelFuture future = ctx.writeAndFlush(resp);
//            if (sendreport) {
//                SgipReportRequestMessage report = new SgipReportRequestMessage();
//                report.setSequenceId(resp.getSequenceNumber());
//                ctx.writeAndFlush(report);
//            }
        } else if (msg instanceof SgipReportRequestMessage) {
            SgipReportRequestMessage reportRequestMessage = (SgipReportRequestMessage) msg;
            log.info("SgipReportRequestMessage: {}", reportRequestMessage);

            SgipReportResponseMessage resp =
                    new SgipReportResponseMessage(((SgipReportRequestMessage) msg).getHeader());
            resp.setResult((short) 0);
            resp.setTimestamp(((SgipReportRequestMessage) msg).getTimestamp());
            ctx.channel().writeAndFlush(resp);

            int status = 0;
            if (Convert.toInt(reportRequestMessage.getState()) == 0) {
                status = 0;
            } else if (Convert.toInt(reportRequestMessage.getState()) == 0) {
                status = 2;
            } else {
                status = 1;
            }

            // 状态报告处理
            // 组装状态报告日志
            ChannelReportMessage channelReportMessage =
                    new ChannelReportMessage(reportRequestMessage.getSequenceNumber().toString(),
                            null,
                            1,
                            null,
                            String.valueOf(reportRequestMessage.getErrorcode()),
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


        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
